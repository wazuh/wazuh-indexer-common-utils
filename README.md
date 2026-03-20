<p align="center">
    <img width="640px" src="https://wazuh.com/brand-assets/Wazuh-Logo.svg"/>
</p>

[![Chat](https://img.shields.io/badge/chat-on%20forums-blue)](https://groups.google.com/forum/#!forum/wazuh)
[![Slack](https://img.shields.io/badge/slack-join-blue.svg)](https://wazuh.com/community/join-us-on-slack)
[![Documentation](https://img.shields.io/badge/documentation-reference-blue)](https://documentation.wazuh.com)

- [Wazuh Indexer Common Utils](#wazuh-indexer-common-utils)
- [Project Resources](#project-resources)
- [Contributing](#contributing)
- [Security](#security)
- [License](#license)
- [Copyright](#copyright)

## Wazuh Indexer Common Utils

The **Wazuh Indexer Common Utils** is a foundational library focused on providing reusable Java components for Wazuh Indexer plugins.

This repository is an open-source fork of the [OpenSearch common-utils](https://github.com/opensearch-project/common-utils) project, adapted to ensure seamless integration within the Wazuh ecosystem.

### Key Components

This library is composed of the following parts:

1. **`SecureRestClientBuilder`**: Provides methods to create secure low-level and high-level REST clients. Essential for making secure REST calls to Wazuh Indexer or other plugin APIs.
2. **`InjectSecurity`**: Provides methods to inject users or roles, which is useful for running background jobs securely.
3. **`IntegTestsWithSecurity`**: Provides methods to create users and roles for running integration tests with the security plugin.
4. **Shared Transport Classes**: Shared request/response/action classes used for plugin-to-plugin transport layer calls.
5. **Common Functionality**: Any common logic across Wazuh Indexer plugins is centralized here to reduce duplication.

## Project Resources

* [Project Website](https://wazuh.com)
* [Documentation](https://documentation.wazuh.com)
* Need help? Try [Slack](https://wazuh.com/community/join-us-on-slack)

## Contributing

See [CONTRIBUTING.md](./CONTRIBUTING.md) and join in. We welcome bug reports and feature requests through GitHub issues.

## Code of Conduct

This project has adopted the [Amazon Open Source Code of Conduct](CODE_OF_CONDUCT.md).

## Security

To report a possible vulnerability or security issue, please email us at **security@wazuh.com** or open a report under the Security tab. **PLEASE DO NOT OPEN A PUBLIC ISSUE.**

## License

This project is licensed under the [Apache v2.0 License](LICENSE.txt).

## Copyright

Copyright Wazuh, Inc. (Original code Copyright OpenSearch Contributors). See [NOTICE](NOTICE.txt) for details.
