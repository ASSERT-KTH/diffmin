# diffmin

In a software development process based on code reviews, the visualization of 
the diff between the base, and the target branch is of utmost importance. It is
known that spurious formatting changes pollute code reviews and lead to
unnecessary communication and code review work. Today, the only solution for
minimizing the diff is one that ignores white spaces. This project will aim to
automatically minimize pull requests by keeping only the AST changes.

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

Licensed under the MIT License. See the `[LICENSE.md](LICENSE.md)` file for more
details.

### Issues

Issues such as bug reports, feature requests, enhancements, or questions can be
reported at the [issues tab](https://github.com/SpoonLabs/diffmin/issues) of
this repository. We don't follow an exact template but please explain your query
as much as possible.

### Pull requests

We welcome pull requests for this project and are always looking forward to
review them.

You can these steps to quickly submit a pull request.

1. Fork the repository.
2. Branch out to `a-meaningful-branch-name`.
3. Make your changes and commit them along with a good summary of the changes as
   the commit message. Refer to the [next subheading](#commit-message-guidelines)
   for the commit message guidelines.
3. Submit your pull request describing the changes introduced in the PR
   description.
4. Get your pull request merged!

### Commit message guidelines

- Commit messages must have a subject line and may have body copy. These must be
  separated by a blank line.
- Make sure the message is concise and not very long. A good commit message is
  ususally atmost 80 characters long.
- The subject line should be capitalised and must not end in a period (.).
- The subject line must be written in imperative mood (*Fix*, not *Fixed* /
  *Fixes* etc.)
- The body copy must only contain explanations as to *what* and *why*, never
  *how*. The latter belongs in documentation and implementation.

## Authors

* [Aman Sharma](https://github.com/algomaster99)
* [Martin Monperrus](https://github.com/monperrus)
* [Simon Larsén](https://github.com/slarse)
