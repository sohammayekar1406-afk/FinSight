package com.ledgerlens.dto;

import java.util.List;

/**
 * Phase 3.5: Evidence Graph
 * 
 * Represents the complete evidence graph for a financial exception.
 * Contains all retrieved evidence nodes and their relationships.
 */
public class EvidenceGraphDto {

    private List<EvidenceNodeDto> nodes;
    private String transactionFlow;  // Human-readable flow description
    private int totalNodesRetrieved;
    private int foundNodes;
    private int missingNodes;

    public EvidenceGraphDto() {}

    public EvidenceGraphDto(List<EvidenceNodeDto> nodes, String transactionFlow,
                           int totalNodesRetrieved, int foundNodes, int missingNodes) {
        this.nodes = nodes;
        this.transactionFlow = transactionFlow;
        this.totalNodesRetrieved = totalNodesRetrieved;
        this.foundNodes = foundNodes;
        this.missingNodes = missingNodes;
    }

    public static EvidenceGraphDtoBuilder builder() { return new EvidenceGraphDtoBuilder(); }

    public List<EvidenceNodeDto> getNodes() { return nodes; }
    public void setNodes(List<EvidenceNodeDto> nodes) { this.nodes = nodes; }

    public String getTransactionFlow() { return transactionFlow; }
    public void setTransactionFlow(String transactionFlow) { this.transactionFlow = transactionFlow; }

    public int getTotalNodesRetrieved() { return totalNodesRetrieved; }
    public void setTotalNodesRetrieved(int totalNodesRetrieved) { this.totalNodesRetrieved = totalNodesRetrieved; }

    public int getFoundNodes() { return foundNodes; }
    public void setFoundNodes(int foundNodes) { this.foundNodes = foundNodes; }

    public int getMissingNodes() { return missingNodes; }
    public void setMissingNodes(int missingNodes) { this.missingNodes = missingNodes; }

    public static class EvidenceGraphDtoBuilder {
        private List<EvidenceNodeDto> nodes;
        private String transactionFlow;
        private int totalNodesRetrieved;
        private int foundNodes;
        private int missingNodes;

        public EvidenceGraphDtoBuilder nodes(List<EvidenceNodeDto> nodes) { this.nodes = nodes; return this; }
        public EvidenceGraphDtoBuilder transactionFlow(String transactionFlow) { this.transactionFlow = transactionFlow; return this; }
        public EvidenceGraphDtoBuilder totalNodesRetrieved(int totalNodesRetrieved) { this.totalNodesRetrieved = totalNodesRetrieved; return this; }
        public EvidenceGraphDtoBuilder foundNodes(int foundNodes) { this.foundNodes = foundNodes; return this; }
        public EvidenceGraphDtoBuilder missingNodes(int missingNodes) { this.missingNodes = missingNodes; return this; }

        public EvidenceGraphDto build() {
            return new EvidenceGraphDto(nodes, transactionFlow, totalNodesRetrieved, foundNodes, missingNodes);
        }
    }
}
