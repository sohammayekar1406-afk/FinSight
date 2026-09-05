# Auto-Investigation Bug Diagnosis

**Date**: September 5, 2026  
**Issue**: Investigations appearing automatically when opening exception detail pages

---

## Executive Summary

**ROOT CAUSE IDENTIFIED:**

The backend code DOES automatically create investigations in ONE specific scenario:

**When clicking "Resolve Exception", "Approve", "Reject", or "Escalate" buttons, if NO investigation exists, the system automatically creates one by calling `investigateException()`.**

This triggers:
- Evidence Graph collection
- Historical RAG retrieval  
- Gemini AI analysis
- Investigation persistence

---

## Detailed Analysis

### Code Locations

**File**: `src/main/java/com/ledgerlens/service/InvestigationService.java`

#### Line 313 - resolveExceptionManually()
```java
if (existingOpt.isPresent()) {
    investigation = existingOpt.get();
    investigation.setActionTaken(ActionTaken.MANUALLY_OVERRIDDEN);
    investigationRepository.save(investigation);
} else {
    // BUG: Automatically creates investigation if none exists!
    InvestigationResponseDto initial = investigateException(exceptionId);
    investigation = investigationRepository.findByException_ExceptionIdAndException_MerchantId(exceptionId, merchantContext.merchantId())
            .orElseThrow(() -> new ResourceNotFoundException("Investigation for exception " + exceptionId + " was not found"));
    investigation.setActionTaken(ActionTaken.MANUALLY_OVERRIDDEN);
    investigationRepository.save(investigation);
}
```

#### Line 366 - approveException()
```java
else {
    investigateException(exceptionId); // Auto-investigates!
    investigation = investigationRepository.findByException_ExceptionIdAndException_MerchantId(exceptionId, merchantContext.merchantId())
            .orElseThrow(() -> new ResourceNotFoundException("Investigation for exception " + exceptionId + " was not found"));
    // ... rest of logic
}
```

#### Line 419 - rejectException()
```java
else {
    investigateException(exceptionId); // Auto-investigates!
    // ... rest of logic
}
```

#### Line 468 - escalateException()
```java
else {
    investigateException(exceptionId); // Auto-investigates!
    // ... rest of logic
}
```

---

## User Workflow Analysis

### Scenario A: User Clicked "Resolve Exception" Without Investigating First

```
1. User runs reconciliation
   → Exceptions created with status=OPEN
   → NO investigations created ✅

2. User opens Exception A detail page
   → Fetches exception data
   → Attempts to fetch investigation via GET /api/investigations/{A}
   → Returns 404 (no investigation found) ✅
   → UI shows "No AI investigation has been run on this exception yet" ✅

3. User clicks "Resolve Exception" button (ADMIN only)
   → POST /api/investigations/{A}/resolve
   → Backend checks if investigation exists
   → Investigation does NOT exist
   → Backend automatically calls investigateException(A) ❌
   → Full AI investigation runs:
      - Evidence Graph built
      - RAG retrieval executed
      - Gemini analysis performed
      - Investigation persisted
   → Exception status set to RESOLVED_MANUAL
   → User sees investigation results ❌

4. User opens Exception B detail page
   → Fetches exception data for B
   → Attempts to fetch investigation via GET /api/investigations/{B}
   → Returns 404 (B has not been resolved/investigated)
   → UI shows "No AI investigation" ✅
   → Correct behavior for B
```

**Conclusion**: If user clicked "Resolve" on A, then A WILL have an investigation. But B will NOT show A's investigation (correct isolation).

### Scenario B: User Clicked "Run All Investigations" Previously

```
1. User runs reconciliation
   → 10 exceptions created (all status=OPEN)

2. User navigates to Investigations page
   → Clicks "Run All Investigations" button
   → POST /api/investigations/run
   → Backend investigates ALL 10 exceptions
   → All 10 investigations persisted

3. User returns to Dashboard, clicks "Run Reconciliation" again
   → 5 NEW exceptions created (status=OPEN)
   → OLD 10 exceptions still exist with investigations

4. User opens old Exception A
   → GET /api/investigations/{A}
   → Returns 200 with investigation data (created in step 2)
   → UI displays existing investigation ✅ CORRECT

5. User opens new Exception B (from step 3)
   → GET /api/investigations/{B}
   → Returns 404 (not yet investigated)
   → UI shows "No investigation" ✅ CORRECT
```

**Conclusion**: Old exceptions retain their investigations. New exceptions do not.

### Scenario C: Demo Validation Was Run

```
1. User clicks "Settings" page
2. User clicks "Run E2E Validation" button
   → POST /api/demo/validate
   → DemoValidationService.runValidation() executes:
      a. Seed demo data
      b. Run reconciliation
      c. **investigateAllOpenExceptions()** ❌
      d. Verify audit trail
      e. Collect stats

3. Result: ALL exceptions now have investigations
4. User navigates to any exception
   → Investigation exists and displays ✅ (but created automatically by validation)
```

**Conclusion**: Demo validation is INTENDED to auto-investigate for testing purposes. Should NOT be used in production workflow.

---

## Frontend Verification

### ExceptionDetailPage.tsx

**Line 37:**
```typescript
const { data: investigation, refetch: refetchInv } = useInvestigationDetail(exceptionId)
```

This calls `GET /api/investigations/{exceptionId}` which:
- Returns 200 + data if investigation EXISTS
- Returns 404 if investigation does NOT exist
- Does NOT create an investigation (read-only)

**Line 211:**
```typescript
{investigation && (
  <SectionCard title="AI Investigation Report" ...>
    {/* Display investigation results */}
  </SectionCard>
)}
```

This ONLY displays investigation IF the `investigation` variable is defined (i.e., API returned 200).

**Conclusion**: Frontend correctly displays existing investigations and hides section if none exists.

### React Query Cache

**Cache Key**: `["investigations", "detail", exceptionId]`

This ensures:
- Exception A's investigation cached separately from Exception B
- No cross-contamination
- Cache correctly scoped

**Conclusion**: No frontend caching bug.

---

## Backend Verification

### GET /api/investigations/{exceptionId}

**InvestigationService.getInvestigation()** - Line 245:

```java
Investigation investigation = investigationRepository.findByException_ExceptionIdAndException_MerchantId(exceptionId, merchantContext.merchantId())
        .orElseThrow(() -> new ResourceNotFoundException("Investigation for exception " + exceptionId + " was not found"));
```

**Throws exception if not found** - does NOT create investigation.

**Conclusion**: GET endpoint is read-only and correct.

### POST /api/investigations/{exceptionId}

**InvestigationService.investigateException()** - Line 91:

Creates new investigation explicitly. Only called when:
1. User clicks "Run AI Investigation" button
2. User clicks "Run All Investigations" button
3. User clicks "Resolve/Approve/Reject/Escalate" WITHOUT existing investigation ❌
4. Demo validation runs ❌

**Conclusion**: POST endpoint correctly requires explicit user action (except cases 3-4).

---

## Actual Bugs Found

### Bug 1: Auto-Investigation on Resolve/Approve/Reject/Escalate

**Severity**: MEDIUM

**Description**: When ADMIN clicks "Resolve Exception", "Approve", "Reject", or "Escalate", if NO investigation exists, the system automatically creates one by running full AI analysis including Evidence Graph, RAG, and Gemini.

**Expected Behavior**: These actions should REQUIRE an existing investigation or should clearly prompt the user that an investigation will be created.

**Impact**: 
- User thinks they're just resolving/approving an exception
- System secretly runs expensive AI analysis
- Creates investigations the user didn't explicitly request
- Consumes Gemini API quota unexpectedly

**Fix Options**:

**Option A**: Remove Auto-Investigation (Recommended)
```java
// Line 313 in resolveExceptionManually()
if (existingOpt.isPresent()) {
    investigation = existingOpt.get();
    investigation.setActionTaken(ActionTaken.MANUALLY_OVERRIDDEN);
    investigationRepository.save(investigation);
} else {
    // NEW: Throw exception instead of auto-creating
    throw new IllegalStateException(
        "Cannot resolve exception without investigation. Please run 'Investigate' first."
    );
}
```

**Option B**: Make Auto-Investigation Explicit
- Add checkbox: "☐ Run AI investigation before resolving"
- Only auto-investigate if checkbox is checked
- Default: unchecked

**Option C**: Split into Two Buttons
- "Resolve Without Investigation" - just sets status
- "Investigate & Resolve" - runs investigation first

### Bug 2: Demo Validation Auto-Investigates

**Severity**: LOW (by design for testing)

**Description**: `POST /api/demo/validate` automatically investigates ALL open exceptions as part of end-to-end validation.

**Expected Behavior**: This is CORRECT for demo/testing, but should NOT be exposed or auto-called in production.

**Fix**: 
1. Restrict endpoint to test environment only: `@Profile("test")`
2. Remove from production builds
3. Document clearly that this is test-only

---

## Scenarios That Are NOT Bugs

### "Investigation appears immediately after reconciliation"

**If**: User previously clicked "Run All Investigations" OR "Resolve" on those exceptions
**Then**: Investigations are persisted in database
**Result**: Opening exception detail page displays existing investigations
**Verdict**: ✅ CORRECT BEHAVIOR

### "Investigation from A appears on B"

**If**: React Query cache keys include exceptionId: `["investigations", "detail", exceptionId]`
**And**: Backend queries by exceptionId AND merchantId
**Then**: Cross-contamination is impossible
**Verdict**: ✅ NOT A BUG (unless user can provide network logs showing otherwise)

### "Investigations auto-created during reconciliation"

**ReconciliationService.runReconciliation()**: Does NOT call InvestigationService
**Verdict**: ✅ NOT A BUG

### "GET endpoint creates investigations"

**InvestigationService.getInvestigation()**: Throws ResourceNotFoundException if not found
**Verdict**: ✅ NOT A BUG

---

## Recommendations

### Immediate Fixes Required

1. **Remove auto-investigation from resolve/approve/reject/escalate**
   - Require existing investigation
   - Throw clear error if missing
   - Update frontend to handle error gracefully

2. **Restrict demo validation to test environment**
   - Add `@Profile("test")` annotation
   - Document that it auto-investigates

3. **Add UI warning for "Run All Investigations"**
   - Show count of exceptions that will be investigated
   - Estimate cost/time
   - Require confirmation

### Documentation Updates

1. **Add to docs/investigation-architecture.md**:
   - List all auto-investigation scenarios
   - Clarify resolve/approve behavior
   - Document demo validation behavior

2. **Update API documentation**:
   - `POST /investigations/{id}/resolve` - Note: Creates investigation if missing
   - `POST /api/demo/validate` - Note: Test environment only, auto-investigates ALL

3. **Add troubleshooting guide**:
   - How to check if investigations exist in database
   - How to verify React Query cache
   - How to clear all investigations for fresh start

### Testing Requirements

1. **Test: Resolve without investigation throws error**
2. **Test: Approve without investigation throws error**
3. **Test: Reject without investigation throws error**
4. **Test: Escalate without investigation throws error**
5. **Test: Resolve WITH investigation succeeds**
6. **Test: GET investigation returns 404 for non-existent**
7. **Test: POST investigation creates new investigation**
8. **Test: Investigation A does not appear for Exception B**

---

## Conclusion

**Primary Bug**: Auto-investigation on resolve/approve/reject/escalate actions

**Secondary Issue**: Demo validation auto-investigates (by design, but needs documentation/restriction)

**Not Bugs**: 
- Reconciliation (correct - no auto-investigation)
- GET endpoint (correct - read-only)
- Frontend caching (correct - properly scoped)
- Displaying existing investigations (correct - they were created explicitly)

**User Observation Explanation**:
If user sees investigations "immediately after reconciliation", most likely:
1. They clicked "Resolve" which auto-investigated ❌
2. They clicked "Run All Investigations" previously ✅
3. Demo validation was run ❌
4. Database has old investigations from previous sessions ✅

**Fix Priority**: 
1. HIGH: Remove auto-investigation from resolve/approve/reject/escalate
2. MEDIUM: Restrict demo validation to test environment
3. LOW: Add UI warnings and documentation
