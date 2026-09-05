# Investigation Architecture - Explicit User Action Required

## Design Principle

**Investigations are NEVER created automatically. They MUST be explicitly triggered by user action.**

---

## Data Flow

### ✅ CORRECT: User-Initiated Investigation

```
User clicks "Run AI Investigation" button
    ↓
POST /api/investigations/{exceptionId}
    ↓
InvestigationService.investigateException(exceptionId)
    ↓
1. Find exception by exceptionId + merchantId
2. Build Evidence Graph via EvidenceCollectionService
3. Retrieve historical RAG cases via SemanticHistoricalRetrievalService
4. Analyze via GeminiAiInvestigationAnalyzer
5. Persist Investigation entity
6. Return InvestigationResponseDto
    ↓
Frontend displays investigation results
```

### ✅ CORRECT: Viewing Existing Investigation

```
User opens Exception Detail Page
    ↓
GET /api/investigations/{exceptionId}
    ↓
InvestigationService.getInvestigation(exceptionId)
    ↓
1. Find exception by exceptionId + merchantId
2. Find EXISTING investigation or throw ResourceNotFoundException
3. Build evidence context for display
4. Return InvestigationResponseDto
    ↓
Frontend displays EXISTING investigation (if found)
    OR
Frontend shows "No Investigation Report Found" (if not found)
```

### ❌ WRONG: Auto-Investigation

```
❌ Reconciliation does NOT create investigations
❌ Seed data does NOT create investigations
❌ Opening exception detail page does NOT create investigations
❌ GET /api/investigations/{exceptionId} does NOT create investigations
❌ Exception status change does NOT create investigations
❌ @Scheduled jobs do NOT create investigations
❌ @EventListener does NOT create investigations
```

---

## Frontend Behavior

### ExceptionDetailPage.tsx

**On Page Load:**
1. Fetch exception details via `GET /api/exceptions/{exceptionId}`
2. Attempt to fetch investigation via `GET /api/investigations/{exceptionId}`
   - If 404: Display "No AI investigation has been run on this exception yet"
   - If 200: Display existing investigation results

**User Clicks "Run AI Investigation":**
1. Call `POST /api/investigations/{exceptionId}`
2. Investigation is created and persisted
3. Refetch investigation data
4. Display new investigation results

### React Query Cache

**Cache Key Structure:**
```typescript
["investigations", "detail", exceptionId]
```

This ensures:
- Exception A's investigation is cached separately from Exception B
- No cross-contamination between exceptions
- Navigation from A → B → A maintains correct state

---

## Backend Endpoints

### POST /api/investigations/{exceptionId}
- **Purpose**: Create NEW investigation
- **Authorization**: ANALYST, ADMIN
- **Side Effects**: 
  - Creates Investigation entity
  - Calls Evidence Graph service
  - Calls RAG retrieval service
  - Calls Gemini AI service
  - May auto-resolve exception if confidence > 90%
  - May create historical embedding if resolved
  - Sets exception status to INVESTIGATING
  - Logs to audit trail

### GET /api/investigations/{exceptionId}
- **Purpose**: Retrieve EXISTING investigation
- **Authorization**: ALL authenticated users
- **Side Effects**: NONE (read-only)
- **Returns**: 200 with investigation data OR 404 if not found
- **Note**: Builds evidence context for display but does NOT persist investigation

### POST /api/investigations/run
- **Purpose**: Investigate ALL open exceptions for merchant
- **Authorization**: ANALYST, ADMIN
- **Side Effects**: Calls POST /{exceptionId} for each OPEN exception

---

## Common Misconceptions

### "After reconciliation, exceptions show AI investigation automatically"

**Explanation:**
- Reconciliation ONLY creates FinancialException entities with status=OPEN
- If you see investigation results, one of these happened:
  1. You clicked "Run All Investigations" previously
  2. Demo validation (`POST /api/demo/validate`) was run
  3. Someone else investigated those exceptions
- The investigations are PERSISTED in the database
- Opening an exception simply DISPLAYS existing data

**To Verify:**
```sql
-- Check if investigations exist for a specific exception
SELECT * FROM investigations 
WHERE exception_id = (SELECT id FROM exceptions WHERE exception_id = 'exp_xxx');

-- Count total investigations for merchant
SELECT COUNT(*) FROM investigations i
JOIN exceptions e ON i.exception_id = e.id
WHERE e.merchant_id = 'merchant_a';
```

### "Investigation results from Exception A appear on Exception B"

**Root Causes:**
1. **Browser Dev Tools**: React Query devtools may show cached queries misleadingly
2. **Database State**: Both A and B were actually investigated previously
3. **Frontend Bug**: React Query cache corruption (but cache keys include exceptionId, so unlikely)

**To Verify:**
1. Open browser Network tab
2. Navigate to Exception A detail page
3. Check request: `GET /api/investigations/{A_exception_id}`
4. Note the response investigation ID
5. Navigate to Exception B detail page
6. Check request: `GET /api/investigations/{B_exception_id}`
7. Verify the investigation ID is DIFFERENT or 404

---

## Demo Validation Behavior

### POST /api/demo/validate

This endpoint runs a FULL END-TO-END validation:
1. Seed demo data
2. Run reconciliation
3. **Run investigations on ALL open exceptions**
4. Verify audit trail
5. Collect dashboard stats

**IMPORTANT:** If you run demo validation, ALL open exceptions will be investigated and persisted. This is intentional for demo/testing purposes.

**Production:** Do NOT expose or auto-call `/api/demo/validate`

---

## Seed Data vs Investigations

### SeedDataService.seedDemoData()
- Creates: Orders, Payments, Settlements, Fees, Refunds, Adjustments
- Does NOT create: Investigations, Embeddings, Evidence Graphs
- Result: Exceptions will be detected by reconciliation with status=OPEN

### After Seeding + Reconciliation
- Exceptions exist with status=OPEN
- NO investigations exist
- User must click "Investigate" to create investigations

### After Seeding + Reconciliation + "Run All Investigations"
- Exceptions exist with status=INVESTIGATING or RESOLVED_AUTO
- Investigations exist and are persisted
- Opening exception detail page will display existing investigations

---

## Testing Requirements

### Test 1: Reconciliation Does Not Investigate
```java
@Test
public void testReconciliation_DoesNotCreateInvestigations() {
    // Setup: Create order/payment with discrepancy
    // Act: Run reconciliation
    // Assert: Exception created with status=OPEN
    // Assert: ZERO investigations exist
}
```

### Test 2: GET Does Not Create Investigation
```java
@Test
public void testGetInvestigation_ThrowsNotFoundIfMissing() {
    // Setup: Create exception (no investigation)
    // Act: Call GET /api/investigations/{exceptionId}
    // Assert: ResourceNotFoundException thrown
    // Assert: No investigation was created as side effect
}
```

### Test 3: POST Creates Investigation
```java
@Test
public void testInvestigateException_CreatesNewInvestigation() {
    // Setup: Create exception (no investigation)
    // Act: Call POST /api/investigations/{exceptionId}
    // Assert: Investigation created and persisted
    // Assert: Evidence Graph built
    // Assert: RAG retrieval executed (if embeddings exist)
    // Assert: Gemini called (if AI enabled)
}
```

### Test 4: Investigation Isolation
```java
@Test
public void testInvestigations_MerchantIsolation() {
    // Setup: Merchant A exception, Merchant B exception
    // Act: Investigate Merchant A exception
    // Assert: Merchant B cannot retrieve Merchant A investigation
}
```

---

## Troubleshooting

### "I see investigation results immediately after reconciliation"

**Check:**
```bash
# 1. Verify investigations exist in database
psql -c "SELECT e.exception_id, i.id as investigation_id, i.created_at 
FROM investigations i 
JOIN exceptions e ON i.exception_id = e.id 
WHERE e.merchant_id = 'YOUR_MERCHANT_ID' 
ORDER BY i.created_at DESC;"

# 2. Check when investigations were created
# If created_at is BEFORE your reconciliation run, they're old data

# 3. Check if demo validation was run
tail -n 1000 application.log | grep "DemoValidationService"

# 4. Check browser localStorage
# Open DevTools → Application → Local Storage
# Look for any cached investigation data
```

### "Investigation A appears on Exception B page"

**Debug:**
1. Open Browser Network tab
2. Navigate to Exception B detail page
3. Find request: `GET /api/investigations/{B_id}`
4. Check response status:
   - 404: Correct - no investigation shown
   - 200: Check response body `investigation.exceptionId` - should equal B_id
5. If response shows A_id instead of B_id: BACKEND BUG (merchant isolation violated)
6. If response shows B_id but UI displays A data: FRONTEND BUG (React state issue)

### Clean Slate Reset

```bash
# Backend: Delete all investigations
DELETE FROM historical_investigation_embeddings;
DELETE FROM investigations;

# Frontend: Clear React Query cache
# In browser: Hard refresh (Ctrl+Shift+R) or
localStorage.clear();
sessionStorage.clear();

# Verify
SELECT COUNT(*) FROM investigations; -- Should be 0
```

---

## Summary

- **Reconciliation**: Creates exceptions ONLY, never investigations
- **Seed Data**: Creates transactions ONLY, never investigations
- **GET /api/investigations/{id}**: Retrieves existing, never creates
- **POST /api/investigations/{id}**: Creates new investigation (explicit user action)
- **Demo Validation**: Auto-investigates ALL exceptions (test/demo only)
- **Frontend**: Displays existing investigations if found, prompts user to investigate if not
- **Cache**: Properly scoped by exceptionId, no cross-contamination
- **Production**: Never auto-investigate, always require explicit user click

**If you see investigation results "automatically", they were created earlier and persisted.**
