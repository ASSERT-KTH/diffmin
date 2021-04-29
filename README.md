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

### Build locally

Run the following command which will build a single jar file for this project
and its dependencies.

```sh
mvn compile assembly:single
```

### Usage

Once the jar is built, the main method can be invovked using just one command.

```sh
java -jar target/diffmin-1.0-SNAPSHOT-jar-with-dependencies.jar <prev.java> <new.java>
```

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
