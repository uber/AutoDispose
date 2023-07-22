# Maintaining Documentation Website

The [documentation website](https://uber.github.io/AutoDispose/) is built using [MkDocs](https://squidfunk.github.io/mkdocs-material/). This requires you to install python to deploy the website. MkDocs supports Python versions 2.7, 3.4, 3.5, 3.6, 3.7.

## Overview

The main configuration of the website happens through the [mkdocs.yml](https://github.com/uber/AutoDispose/blob/main/mkdocs.yml) file. This defines various things like extra css, favicons, icons as well as the main navigation of the website.

## Adding a Page

All of the main pages exist in [docs](https://github.com/uber/AutoDispose/tree/main/docs) directory. To add a new page, simply add a new markdown file to the docs directory. Once that's done, simply add the title of the page and the name of the file in the `nav` section of mkdocs.yml like so:
```yml
nav:
  - 'New Page': new_page.md
    - 'Nested Page': nested_page.md
  -- 'Top Level Page': top_level_page.md
 ```

 ## Editing a Page

 You can find the markdown file corresponding to the page that you want to edit by checking the `mkdocs.yml` file and finding the `nav` section. This should have the markdown file name.
You can then find that file in the `docs` directory. After you've made changes to that file, get them merged in and [deploy the website](#deploying-the-website)

 ## Adding Custom CSS

 You can add your own CSS in the [app.css](https://github.com/uber/AutoDispose/blob/main/docs/css/app.css) file.

 ## Deploying the website

 The website can be deployed by executing the `deploy_website.sh` script like so:
```bash
./deploy_website.sh
```
The script does the following:
 * Creates a temporary folder.
 * Clones the repository.
 * Runs Dokka.
 * Copies over the files like `README.md`, `CHANGELOG.md`, `CONTRIBUTING.md` etc into the `docs` folder. This is done because these markdown files are required to be in the GitHub repo and we copy it over so that we don't create duplicates.
 * Deploys mkdocs on gh-pages.
 * Deletes the temporary folder.
