## [v5.0.0]

### Added
- Compatibility with OpenSearch 3.6.0 [(#15)](https://github.com/wazuh/wazuh-indexer-common-utils/issues/15)
- Add the active response notification channel type [(#2)](https://github.com/wazuh/wazuh-indexer-common-utils/issues/2)
- Add a dedicated monitor type for active response [(#8)](https://github.com/wazuh/wazuh-indexer-alerting/issues/8)
- Add an `internalCaller` flag to monitor index requests [(#1589)](https://github.com/wazuh/wazuh-indexer/issues/1589)

- Initialize `wazuh-indexer-common-utils` repository [(#1)](https://github.com/wazuh/wazuh-indexer-common-utils/issues/1)

- (operational) Add the `--set-as-main` flag to the repository bumper [(#9)](https://github.com/wazuh/wazuh-indexer-common-utils/issues/9)
- (operational) Add revert support to the repository bumper workflow [(#24)](https://github.com/wazuh/wazuh-indexer-common-utils/issues/24) [(#82)](https://github.com/wazuh/wazuh-indexer-common-utils/issues/82)
- (operational) Add reporting of skipped bumps to the repository bumper workflow [(#113)](https://github.com/wazuh/wazuh-indexer-common-utils/issues/113)

### Changed
- Allow `keyword` and `match_only_text` fields in custom query index mappings [(#285)](https://github.com/wazuh/wazuh-indexer-security-analytics/issues/285) [(#1529)](https://github.com/wazuh/wazuh-indexer-plugins/issues/1529)
- Allow `{{ field }}` placeholders in doc-level query tags [(#181)](https://github.com/wazuh/wazuh-indexer-security-analytics/issues/181)

- (operational) Update the Maven POM with Wazuh details [(#1439)](https://github.com/wazuh/wazuh-indexer/issues/1439)
- (operational) Resolve the build version from `VERSION.json` [(#1595)](https://github.com/wazuh/wazuh-indexer-plugins/issues/1595)
- (operational) Update CodeQL configuration [(#16)](https://github.com/wazuh/wazuh-indexer-common-utils/issues/16)

### Removed

-

### Fixed
- Fix `AlertingException.wrap()` logging the same failure once per call-stack layer and resetting its status to 500 [(#1867)](https://github.com/wazuh/wazuh-indexer/issues/1867)

- (operational) Fix the repository bumper merge step running with an empty pull request URL [(#23)](https://github.com/wazuh/wazuh-indexer-common-utils/issues/23)
- (operational) Fix the repository bumper `tag` input defaulting to `true` [(#1765)](https://github.com/wazuh/wazuh-indexer/issues/1765)
  
## Prior versions
- []()
