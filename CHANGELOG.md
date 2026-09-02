## [v5.0.0]

### Added

- Define active response channel type [(#2)](https://github.com/wazuh/wazuh-indexer-common-utils/issues/2)
- Support Revert bump functionality in wazuh-indexer-common-utils [(#24)](https://github.com/wazuh/wazuh-indexer-common-utils/issues/24)
- Implement dedicated monitor for Active Response [(#8)](https://github.com/wazuh/wazuh-indexer-alerting/issues/8) [(#61)](https://github.com/wazuh/wazuh-indexer-alerting/issues/61)

### Changed

-

### Removed

-

### Fixed
- CodeQL failures [(#16)](https://github.com/wazuh/wazuh-indexer-common-utils/issues/16)
- `gh pr merge` called with empty URL in `5_bumper_repository.yml` [(#23)](https://github.com/wazuh/wazuh-indexer-common-utils/issues/23)
- Stop `AlertingException.wrap()` re-wrapping and re-logging the same failure once per layer of the unwinding call stack [(#1867)](https://github.com/wazuh/wazuh-indexer/issues/1867) [(#1788)](https://github.com/wazuh/wazuh-indexer/issues/1788)
- No finding generated for a matching detection rule (Intune non-compliant device) [(#285)](https://github.com/wazuh/wazuh-indexer-security-analytics/issues/285)

## Prior versions
- []()
