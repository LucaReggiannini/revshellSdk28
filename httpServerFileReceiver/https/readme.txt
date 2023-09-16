Generate your certificate in this folder with the following commands:

openssl req -newkey rsa:2048 -nodes -keyout bind.key -x509 -days 1000 -out bind.crt
cat bind.key bind.crt > bind.pem
