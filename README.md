# Beeresieb
[![travis.ci](https://api.travis-ci.com/Nachtbeere/Beeresieb.svg?branch=master)](https://travis-ci.com/github/Nachtbeere/Beeresieb)

A Minecraft uuid-base whitelist system for Custom REST API

## Features
* Works with AsyncPlayerPreLoginEvent
    * It doesn't block your main server thread
* Main API and Failsafe API supported
* Dead man's switch function
    * If main and failsafe api all dead, plugin do allow/deny arbitrarily by config

## API Response for Beeresieb
* allowed response format is<code>{ "uuid": :uuid_string, "verified": :bool }</code>
* if there is no player exist, response with<code>{ "uuid": "00000000000000000000000000000000", "verified": :bool }</code>
    * because nil uuid is a 128 bits zero. see the [RFC 4122 reference](https://tools.ietf.org/html/rfc4122#section-4.1.7).
    * or just return <code>404 Not Found</code> Status code.
* <code>verified</code> field for user's e-mail verification check or something for your api.
* the players must have <code>exist in api server</code> and <code>verified in api server</code> to join