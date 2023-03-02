# AWS Lambda

![AWS Lambda](https://upload.wikimedia.org/wikipedia/commons/thumb/5/5c/Amazon_Lambda_architecture_logo.svg/281px-Amazon_Lambda_architecture_logo.svg.png)

Okteto will automatically create (or update, if existing) an AWS :ambda function every time you deploy the development environment. The environment is already configured to read/write credentials to the Lambda function. Every development environment you deploy will have it's own function. The function will be destroyed when you destroy the development environment.

To see the function's dashboard, click on the link in the lambda resource, and log in with your AWS credentials.