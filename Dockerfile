FROM paperist/texlive-ja:latest

RUN apt update \
  && apt install -y texlive-extra-utils openjdk-17-jre-headless poppler-utils