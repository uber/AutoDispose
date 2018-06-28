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
 10. After release propagates (wait ~1 hour), update Javadocs/KDocs via [Osstrich](https://github.com/square/osstrich)
     - Make sure you have push access
     - `./gradlew publishDocs`
