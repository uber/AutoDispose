Releasing
=========

 1. Change the version in `gradle.properties` to a non-SNAPSHOT version.
 2. Update the `CHANGELOG.md` for the impending release.
 3. `git commit -am "Prepare for release X.Y.Z."` (where X.Y.Z is the new version)
 4. `git tag -a X.Y.Z -m "Version X.Y.Z"` (where X.Y.Z is the new version)
 5. `./gradlew clean uploadArchives -Dorg.gradle.parallel=false`
 6. Update the `gradle.properties` to the next SNAPSHOT version.
 7. `git commit -am "Prepare next development version."`
 8. `git push && git push --tags`
 9. Visit [Sonatype Nexus](https://oss.sonatype.org/) and promote the artifact.
     - Select the artifact, click `close`, wait for it to close, then select again and click 
     `release`.
 10. After release propagates (wait ~1 hour), update Javadocs via [Osstrich](https://github.com/square/osstrich)
     - Clone locally
     - `mvn package`
     - `rm -rf tmp/autodispose && java -jar target/osstrich-cli.jar tmp/autodispose git@github.com:uber/autodispose.git com.uber.autodispose`
       - Note this step may [take a couple tries](https://github.com/square/osstrich/issues/17)
       - If [this issue](https://github.com/square/osstrich/issues/18) is still open, edit the generated top-level `index.html` file to fix it and then push the fix after.
         - To fix, adjust every kotlin artifact's `href` to point to the subdirectory of the same artifact name
         - e.g. `<a href="autodispose-kotlin">autodispose-kotlin</a>` -> `<a href="autodispose-kotlin/autodispose-kotlin">autodispose-kotlin</a>`
