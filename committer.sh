#!/usr/bin/env bash
mvn clean install -U;
git add .; git commit -m "`date '+%Y-%m-%d %H:%M:%S'`"; git push
