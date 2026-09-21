# Catalog Service Deployment Guide

This document describes how to use and apply the deployment process.


## Navigation

  - [Design](#design)
  - [Production Deployments](#production-deployments)
  - [Example Build](#example-build)
  - [Example Run](#example-run)


## Design

The **Docker** files are designed to be operated by *Developers*, *Developer Operations*, and *Operations*.
Each operator group has different wants and needs.
These different needs are accommodated using build time arguments and local configuration files.

The **Docker** images are based on appropriate upstream images based on desired functionality.
The specific versions of the software can be toggled to allow for performing some amount of updates without requiring code changes.
This should be very useful for security updates.

*Developers* tend to have their projects already built.
*Developers* also have very little need to perform system updates in the **Docker** images.
Environment variables are exposed to allow customizing this behavior.
The default behavior, however, favors production build to ensure that the final image stage has the latest code.
This default behavior favors good security practices.

*Operations* and *Developer Operations* often do not have the files pre-built.
Furthermore, they are less likely to know or care about such a process.
Environment variables are provided to automatically perform these tasks.
Such images take more time and resources to build, but it saves the user from having to know all of the technical repository specific build needs.


## Production Deployments

For **production** deployments, deployment should ideally be done using `docker-compose`.
However, there is currently no *App* repository for *Catalog Service* providing a `docker-compose` file.

For now, **production** deployments are identical to **development** deployments as described in the **Docker** method below.

<div align="right">(<a href="#readme-top">back to top</a>)</div>


## Example Build

Either `docker` or `podman` may be used to build.
The commands are generally interchangeable.

Both **Build Variables** and **Environment Variables** are described in the [variable.md](variable.md) documentaton.

To build the server without bundling or updating anything:
```sh
docker build --tag tamu/catalog_server/manual --build-arg UPDATE_MAVEN="false" --build-arg UPDATE_SERVE="false" --build-arg BUILD_JAR="false" .
```

To build the server updating and building everything:
```sh
docker build --tag tamu/catalog_server/build --build-arg UPDATE_MAVEN="true" --build-arg UPDATE_SERVE="true" --build-arg BUILD_JAR="true" .
```


## Example Run

The named image can be run once the image is built using either `docker` or `podman`.

The following is an example using the "build the server updating and building everything" example that has tag `tamu/catalog_server/build`:

```shell
docker run -it tamu/catalog_server/build
```
