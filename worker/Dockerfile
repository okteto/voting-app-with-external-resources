FROM okteto/pipeline-runner:1.0.0

WORKDIR /src

RUN apt-get update -y
RUN apt-get install -y unzip wget build-essential libncurses5-dev zlib1g-dev libnss3-dev libgdbm-dev libssl-dev libsqlite3-dev libffi-dev libreadline-dev libbz2-dev
RUN wget https://www.python.org/ftp/python/3.9.7/Python-3.9.7.tgz && \
    tar -xvf Python-3.9.7.tgz && \
	rm -Rf Python-3.9.7.tgz

WORKDIR /src/Python-3.9.7
RUN ./configure --enable-optimizations && \
	make -j 2 && \
	nproc && \
	make altinstall

WORKDIR /src
RUN curl -fSL https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip -o awscliv2.zip && \
	unzip awscliv2.zip && \
	rm -f awscliv2.zip && \
	./aws/install && \
	aws --version

RUN curl -fSL https://github.com/aws/aws-sam-cli/releases/latest/download/aws-sam-cli-linux-x86_64.zip -o aws-sam-cli-linux-x86_64.zip && \
	unzip aws-sam-cli-linux-x86_64.zip -d sam-installation && \
	rm -f aws-sam-cli-linux-x86_64.zip && \
	./sam-installation/install && \
	sam --version
