# Maintaining Documentation Website

The [documentation website](https://uber.github.io/AutoDispose/) is built using [MkDocs](https://squidfunk.github.io/mkdocs-material/). This requires you to install python to deploy the website. 

## Overview

The main configuration of the website happens through the [mkdocs.yml](https://github.com/uber/AutoDispose/blob/master/mkdocs.yml) file. This defines various things like extra css, favicons, icons as well as the main navigation of the website. 

## Adding a Page

All of the main pages exist in [docs](https://github.com/uber/AutoDispose/tree/master/docs) directory. To add a new page, simply add a new markdown file to the docs directory. Once that's done, simply add the title of the page and the name of the file in the `nav` section of mkdocs.yml like so:
```yml
nav:
  - 'New Page': new_page.md
 ```
 
 ## Editing a Page
 
 Find the page that usually exists in the `docs` directory. After you've made the changes, [deploy the website](#deploying-the-website)
 
 ## Adding Custom CSS
 
 You can add your own CSS as well in the [app.css](https://github.com/uber/AutoDispose/blob/master/docs/css/app.css) file.
 
 ## Deploying the website
 
 The website can be deployed by executing the `deploy_website.sh` script. The script does the following:
 * Creates a temporary folder
 * Clones the repository
 * Run Dokka
 * Copy over the files like `README.md`, `CHANGELOG.md`, `CONTRIBUTING.md` etc into the `docs` folder. This is done because these markdown files are required to be in the GitHub repo and we copy it over so that we don't create duplicates. 
 * Deploy mkdocs on gh-pages
 * Delete temp folder
 
