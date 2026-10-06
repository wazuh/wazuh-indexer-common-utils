## [v5.0.0]

### Added

| Issue | Comment |
|-------|---------|
| [#1](https://github.com/wazuh/wazuh-indexer-common-utils/issues/1) | Initialize `wazuh-indexer-common-utils` repository |
| [#15](https://github.com/wazuh/wazuh-indexer-common-utils/issues/15) | Compatibility with OpenSearch 3.6.0 |
| [#2](https://github.com/wazuh/wazuh-indexer-common-utils/issues/2) | Add the active response notification channel type |
| [#8](https://github.com/wazuh/wazuh-indexer-alerting/issues/8) | Add a dedicated monitor type for active response |
| [#1589](https://github.com/wazuh/wazuh-indexer/issues/1589) | Add an `internalCaller` flag to monitor index requests |

### Changed

| Issue | Comment |
|-------|---------|
| [#285](https://github.com/wazuh/wazuh-indexer-security-analytics/issues/285) [#1529](https://github.com/wazuh/wazuh-indexer-plugins/issues/1529) | Allow `keyword` and `match_only_text` fields in custom query index mappings |
| [#181](https://github.com/wazuh/wazuh-indexer-security-analytics/issues/181) | Allow `{{ field }}` placeholders in doc-level query tags |

### Removed

| Issue | Comment |
|-------|---------|

### Fixed

| Issue | Comment |
|-------|---------|
| [#1867](https://github.com/wazuh/wazuh-indexer/issues/1867) | Fix `AlertingException.wrap()` logging the same failure once per call-stack layer and resetting its status to 500 |

## Prior versions
