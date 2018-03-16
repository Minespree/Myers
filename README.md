# Myers

[![Discord](https://img.shields.io/discord/352874955957862402.svg)](https://discord.gg/KUFmKXN)
[![License](https://img.shields.io/github/license/Minespree/Myers.svg)](LICENSE)
![Documentation](https://img.shields.io/badge/docs-missing-red.svg)

This is the code that powered the Myers synchronization app of the former Minespree Network. Myers bridged BungeeCord and PlayPen together by synchronizing data from PlayPen with servers registered in BungeeCord using Redis. Its name comes from the atheist blogger [PZ Myers](https://en.wikipedia.org/wiki/PZ_Myers).

Besides the removal of some branding and configuration data, it is more or less unmodified. It is probably not _directly_ useful to third parties in its current state, but it may be help in understanding how the Minespree network operated.

We are quite open to the idea of evolving this into something more generally useful. If you would like to contribute to this effort, talk to us in [Discord](https://discord.gg/KUFmKXN).

Please note that this project might have legacy code that was planned to be refactored and as so, we kindly ask you not to judge the programming skills of the author(s) based on this single codebase.

## Requirements

To build Myers, the following will need to be installed and available from your shell:

* [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) version 131 or later (older versions _might_ work)
* [Git](https://git-scm.com/)
* [Maven](https://maven.apache.org/)

You can find detailed installation instructions for these tools on the [Getting started](https://github.com/Minespree/Docs/blob/master/setup/DEPENDENCIES.md) docs page.

## Getting started

You can build this project running the following command:

```
mvn package
```

> Please note that you might also want to `mvn install` this module to your local `.m2` repo as it is required by other packages.
> You can also deploy it to your own Maven repo to use the GitLab CI Docker image and make distribution easier. [Instructions](https://github.com/Minespree/Docs/blob/master/deploy/PLAYPEN_DEPLOYER.md)

Next, install the PlayPen plugin by moving the produced artifact on `playpen/target` to your PlayPen `plugins/` directory and restart the network. Once starter, add your Redis credentials to the `config.json` file.

Finally, install the BungeeCord plugin by moving the produced artifact on `bungee/target` to your BungeeCord `plugins/` directory. Restart the proxy and add your Redis credentials to the `config.yml` file again.

Once you're done, just provision servers in PlayPen and Myers will reflect them.

By default, all servers provisioned will be added to Myers. If you want to omit a server, set the PlayPen string `myers_expose` to `false`.

## Architecture

This repo contains the following components:

* PlayPen plugin to synchronize network data
* Bukkit client to retrieve data from Redis
* Bungee client to retrieve data from Redis

## Authors

This project was maintained by the Minespree Network team. If you have any questions or problems, feel free to reach out to the specific writers and maintainers of this project:

<table>
  <tbody>
    <tr>
      <td align="center">
        <a href="https://github.com/astei">
          <img width="150" height="150" src="https://github.com/astei.png?v=3&s=150">
          </br>
          Tux
        </a>
      </td>
      <td align="center">
        <a href="https://github.com/hugmanrique">
          <img width="150" height="150" src="https://github.com/hugmanrique.png?v=3&s=150">
          </br>
          Hugmanrique
        </a>
      </td>
      <td align="center">
        <a href="https://github.com/exception">
          <img width="150" height="150" src="https://github.com/exception.png?v=3&s=150">
          </br>
          exception
        </a>
      </td>
    </tr>
  <tbody>
</table>

## Coding Conventions

* We generally follow the Sun/Oracle coding standards.
* No tabs; use 4 spaces instead
* No trailing whitespaces
* No CRLF line endings, LF only, put your git's `core.autocrlf` on `true`.
* No 80 column limit or 'weird' midstatement newlines.

## License

Myers is free software: you can redistribute it and/or modify it under the terms of the Apache License, Version 2.0.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

A copy of the Apache License, Version 2.0 is included in the file LICENSE, and can also be found at https://www.apache.org/licenses/LICENSE-2.0
