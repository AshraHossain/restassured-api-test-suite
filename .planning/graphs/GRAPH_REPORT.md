# Graph Report - restassured-api-test-suite  (2026-05-26)

## Corpus Check
- 13 files · ~4,893 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 96 nodes · 107 edges · 8 communities (4 shown, 4 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 8 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 5|Community 5]]
- [[_COMMUNITY_Community 6|Community 6]]

## God Nodes (most connected - your core abstractions)
1. `NegativeEdgeCaseTest` - 11 edges
2. `AuthTests` - 9 edges
3. `CrudWorkflowTest` - 9 edges
4. `SchemaValidationTest` - 8 edges
5. `ContractValidationTest` - 8 edges
6. `REST Assured API Automation Suite` - 8 edges
7. `BaseTest` - 7 edges
8. `ChainedEndpointTest` - 6 edges
9. `TokenStore` - 5 edges
10. `ReqresMockServer` - 5 edges

## Surprising Connections (you probably didn't know these)
- `NegativeEdgeCaseTest` --extends--> `BaseTest`  [EXTRACTED]
  src/test/java/com/apitest/tests/NegativeEdgeCaseTest.java →   _Bridges community 5 → community 1_
- `AuthTests` --extends--> `BaseTest`  [EXTRACTED]
  src/test/java/com/apitest/tests/AuthTests.java →   _Bridges community 1 → community 2_
- `CrudWorkflowTest` --extends--> `BaseTest`  [EXTRACTED]
  src/test/java/com/apitest/tests/CrudWorkflowTest.java →   _Bridges community 1 → community 3_
- `SchemaValidationTest` --extends--> `BaseTest`  [EXTRACTED]
  src/test/java/com/apitest/tests/SchemaValidationTest.java →   _Bridges community 1 → community 6_

## Communities (8 total, 4 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.15
Nodes (3): BaseTest, ExtentReportManager, ReqresMockServer

### Community 1 - "Community 1"
Cohesion: 0.15
Nodes (3): BaseTest, ChainedEndpointTest, ContractValidationTest

### Community 4 - "Community 4"
Cohesion: 0.17
Nodes (11): Author, CI Pipeline, code:block1 (restassured-api-test-suite/), code:bash (# Clone), code:block3 (push / PR → checkout → setup JDK 17 → mvn test → upload Exte), Features, Project Structure, REST Assured API Automation Suite (+3 more)

## Knowledge Gaps
- **7 isolated node(s):** `Features`, `Tech Stack`, `code:block1 (restassured-api-test-suite/)`, `code:bash (# Clone)`, `code:block3 (push / PR → checkout → setup JDK 17 → mvn test → upload Exte)` (+2 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **4 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `AuthTests` connect `Community 2` to `Community 1`?**
  _High betweenness centrality (0.155) - this node is a cross-community bridge._
- **Why does `CrudWorkflowTest` connect `Community 3` to `Community 1`?**
  _High betweenness centrality (0.144) - this node is a cross-community bridge._
- **Why does `NegativeEdgeCaseTest` connect `Community 5` to `Community 1`?**
  _High betweenness centrality (0.115) - this node is a cross-community bridge._
- **What connects `Features`, `Tech Stack`, `code:block1 (restassured-api-test-suite/)` to the rest of the system?**
  _7 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 2` be split into smaller, more focused modules?**
  _Cohesion score 0.14 - nodes in this community are weakly interconnected._