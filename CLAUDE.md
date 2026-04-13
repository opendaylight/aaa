# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

**Requirements:** JDK 21, Maven 3.9.5+

```bash
# Full build (includes karaf distribution by default)
mvn clean install

# Build without karaf
mvn clean install -P-karaf

# Skip tests
mvn clean install -DskipTests

# Build a single module (from its directory)
cd aaa-shiro/impl && mvn clean install

# Run all tests in a module
mvn test -pl aaa-shiro/impl

# Run a specific test class
mvn test -Dtest=TokenStoreTest -pl aaa-tokenauthrealm

# Run a specific test method
mvn test -Dtest=TokenStoreTest#testCreateToken -pl aaa-tokenauthrealm
```

## Code Quality

Pre-commit hooks handle formatting and linting:

```bash
pip install pre-commit
pre-commit install
pre-commit run --all-files   # Run manually on all files
```

The `.pre-commit-config.yaml` enforces trailing whitespace, YAML/XML/JSON validity, no tabs, and Prettier for YAML/Markdown.

## Architecture

AAA (Authentication, Authorization, Accounting) is an OpenDaylight Karaf plugin deployed as OSGi bundles.

**Request flow:**

```
HTTP Request
  → Shiro Filter Chain (aaa-shiro)
    → Authentication Realm (TokenAuthRealm, LDAP, Keystone, etc.)
      → Identity Store (H2 – aaa-idm-store-h2) [for local auth]
    → Authorization Filter (MDSALDynamicAuthorizationFilter)
  → Protected Resource
```

**Key modules:**

| Module                      | Role                                                                   |
| --------------------------- | ---------------------------------------------------------------------- |
| `aaa-authn-api`             | Core authentication interfaces (`Claim`, `TokenStore`, etc.)           |
| `aaa-shiro`                 | Apache Shiro integration — servlet filters, realm wiring, web security |
| `aaa-tokenauthrealm`        | Local H2-backed realm; provides OOB users/roles/domains                |
| `aaa-idm-store-h2`          | H2 file-based identity store (users, roles, domains, grants)           |
| `aaa-password-service`      | Password hashing/validation (api + impl split)                         |
| `aaa-encrypt-service`       | Encryption/decryption service (api + impl split)                       |
| `aaa-cert`                  | Certificate management                                                 |
| `aaa-filterchain`           | Configurable javax.servlet filter chain                                |
| `aaa-jetty-auth-log-filter` | Logs authentication events via Jetty                                   |
| `aaa-cli` / `aaa-cli-jar`   | CLI tools for pre-installing identity data                             |
| `web`                       | HTTP server abstraction (Jetty + OSGi impls)                           |
| `features`                  | Karaf feature XML descriptors for installing bundles                   |
| `parent`                    | Maven parent POM with all dependency management                        |
| `artifacts`                 | BOM artifact for consumers                                             |

**Two authorization engines:**

- `MDSALDynamicAuthorizationFilter` — recommended; stores policies in MD-SAL datastore, supports runtime updates, URL-relative to servlet root (`/rests/...`)
- `RolesAuthorizationFilter` (deprecated) — configured in `aaa-app-config.xml`, only re-read on restart, URL-relative to servlet context (ambiguous)

**Two local identity realms (use only one):**

- `TokenAuthRealm` — H2-backed, creates default `admin/admin` credentials OOB
- `MdsalRealm` — MD-SAL backed, ships empty (no OOB credentials)

## Caveats

- The H2 identity store is **not cluster-aware**: each node maintains independent local credentials.
- `aaa-app-config.xml` (Shiro configuration) is only re-read on container restart; dynamic policy changes must use `MDSALDynamicAuthorizationFilter`.
- Default OOB credentials (admin/admin on the "sdn" domain) are intentional bootstrapping aids — the `aaa-cli-jar` module exists specifically to replace them during installation.
- H2 database itself is protected with default credentials (`foo`/`bar`), configurable via `etc/org.opendaylight.aaa.h2.cfg`.

## OSGi / Karaf Deployment

Modules use the `api`/`impl` split pattern so OSGi can wire implementations at runtime. When adding a new service:

- Define interfaces in an `api` submodule
- Implement in an `impl` submodule with `@Component` (Blueprint or DS annotations)
- Register the feature in `features/` so Karaf can install the bundle

Logging for auth events is via slf4j; to enable auth attempt logging in a running Karaf:

```
karaf> log:set DEBUG org.opendaylight.aaa.shiro.filters.AuthenticationListener
karaf> log:set TRACE org.opendaylight.aaa   # full debug
```
