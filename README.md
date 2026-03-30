# digitalai-agility-tools

Tools for the Digital.AI Agility platform.

## Installation

### Download

Download the latest native binary from [releases](https://github.com/marshalhayes/digitalai-agility-tools/releases).

### Configuration

1. Create an access token in Agility
2. Create `~/.agility/config.json` with your credentials:

```json
{
  "url": "https://your-instance.agility.com",
  "accessToken": "your-access-token"
}
```

Alternatively, you can set `AGILITY_URL` and `AGILITY_ACCESS_TOKEN` in your environment.

## Building from Source

### Prerequisites

- [GraalVM JDK 25+](https://www.graalvm.org/downloads/)

### Build

```bash
./mvnw -pl cli -am -Pnative package
```

The native binary will be available at `cli/target/agility`.
