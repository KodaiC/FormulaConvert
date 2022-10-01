FROM paperist/texlive-ja:latest

RUN apt update \
    && apt install -y gnupg gnupg2 gnupg1 software-properties-common

RUN wget https://apt.corretto.aws/corretto.key \
    && apt-key add corretto.key \
    && add-apt-repository 'deb https://apt.corretto.aws stable main'

RUN apt update \
    && apt install -y texlive-extra-utils java-17-amazon-corretto-jdk poppler-utils

EXPOSE 8080
