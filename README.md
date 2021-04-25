# diffmin

In a software development process based on code reviews, the visualization of the diff between the base, and the target
branch is of utmost importance. It is known that spurious formatting changes pollute code reviews and lead to
unnecessary communication and code review work. Today, the only solution for minimizing the diff is one that ignores
white spaces. This project will aim to automatically minimize pull requests by keeping only the AST changes.

## Getting Started

This package is not published yet but one can build the project from source.

### Prerequisites

1. Java 11+ JDK
2. Maven
3. Git

### Installing

1. Clone the repository.
    ```shell
    git clone https://github.com/SpoonLabs/diffmin.git
    ```
2. Install dependencies.
    ```shell
    mvn install
    ```

### Usage

## Running the tests

Tests are under `src/test`. They can be run by executing the following command.

```shell
mvn test
```

## Contributing

### License

Explain the license. see the `[LICENSE.md](LICENSE.md)` file for details

### Issues

### Pull requests

## Authors

* [Aman Sharma](https://github.com/algomaster99)
* [Martin Monperrus](https://github.com/monperrus)
* [Simon Larsén](https://github.com/slarse)
