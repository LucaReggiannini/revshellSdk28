#!/usr/env python3
import http.server, socketserver, socket, ssl
import io
import cgi

# Example: curl -v --insecure -F file=@/home/kali/Downloads/file.bin https://127.0.0.1:4443

'''
To upload files:
#!/usr/bin/sh
u="https://127.0.0.1:4443/"
f=$(basename "$1")
r=$(curl -Iks "$u$f")
c=$(echo "$r" | grep -Po '(?<=HTTP/\d |HTTP/\d\.\d )\d+')
echo $r
echo $c
#l=$(echo "$r" | grep -Po '(?<=Content-Length: )\d+')
if [ $c -ne "200" ]
then
    curl -Fks file=@"$1" $u
fi
'''
IP = "192.168.254.106"
PORT = 4443
PATH_FILES = "./files"

class CustomHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):

    def send_header(self, keyword, value):
        '''
        Method 'send_header' overwritter to prevent "Server" keyword to be sent
        This prevents Banner Grabbind from malicious actors

        Original 'Server' Header example:
            Server: SimpleHTTP/0.6 Python/3.10.9
        Original 'send_header' code: https://github.com/python/cpython/blob/main/Lib/http/server.py
        '''
        if keyword.lower() == 'server':
            return

        if self.request_version != 'HTTP/0.9':
            if not hasattr(self, '_headers_buffer'):
                self._headers_buffer = []
            self._headers_buffer.append((keyword + ": " + value + "\r\n").encode('latin-1', 'strict'))

        if keyword.lower() == 'connection':
            if value.lower() == 'close':
                self.close_connection = True
            elif value.lower() == 'keep-alive':
                self.close_connection = False

    def setHeaders(self):
        '''
        Call this to automatically set the same headers for every type of response.
        Outsiders will have more difficulties to understand what's going on.
        '''
        self.send_response(200)
        self.send_header("Content-type", "text/plain")
        self.send_header("Content-Length", "0")
        self.end_headers()

    def do_HEAD(self):
        '''
        Method 'do_HEAD' overwritten to change requested path before calling 'send_head' function:
        now requested path must be started with const PATH_FILES
        '''
        self.path = PATH_FILES + self.path
        f = self.send_head()
        if f:
            f.close()

    def do_POST(self):
        r, info = self.deal_post_data()
        print(r, info, "from: ", self.client_address)
        self.setHeaders()

    def do_GET(self):
        self.setHeaders()

    def deal_post_data(self):
        try:
            # Parse Header
            ctype, pdict = cgi.parse_header(self.headers['Content-Type'])
            pdict['boundary'] = bytes(pdict['boundary'], "utf-8")
            pdict['CONTENT-LENGTH'] = int(self.headers['Content-Length'])

            # Check parsed Header
            if ctype == 'multipart/form-data':
                form = cgi.FieldStorage(
                    fp=self.rfile,
                    headers=self.headers,
                    environ={
                        'REQUEST_METHOD':'POST',
                        'CONTENT_TYPE':self.headers['Content-Type'], 
                        })

                try:
                    if isinstance(form["file"], list):
                        # If 'form["file"]' is a list -> we have multiple files
                        for record in form["file"]:
                            open(PATH_FILES + "/" + record.filename, "wb").write(record.file.read())
                    else:
                        # If 'form["file"]' is not a list -> we have a single file
                        open(PATH_FILES + "/" + form["file"].filename, "wb").write(form["file"].file.read())
                except:
                        return (False, "ERROR: write error")
            else:
                return (False, "ERROR: bad request")
            return (True, "SUCCESS: files uploaded")
        except:
            return (False, "ERROR: bad request")

Handler = CustomHTTPRequestHandler
https = socketserver.TCPServer((IP, PORT), Handler)
https.socket = ssl.wrap_socket(https.socket, keyfile='./https/bind.key', certfile='./https/bind.crt', server_side=True, ssl_version=ssl.PROTOCOL_TLSv1_2, ca_certs=None, do_handshake_on_connect=True, suppress_ragged_eofs=True, ciphers='ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-GCM-SHA384:ECDHE-ECDSA-AES256-GCM-SHA384:DHE-RSA-AES128-GCM-SHA256:DHE-DSS-AES128-GCM-SHA256:kEDH+AESGCM:ECDHE-RSA-AES128-SHA256:ECDHE-ECDSA-AES128-SHA256:ECDHE-RSA-AES128-SHA:ECDHE-ECDSA-AES128-SHA:ECDHE-RSA-AES256-SHA384:ECDHE-ECDSA-AES256-SHA384:ECDHE-RSA-AES256-SHA:ECDHE-ECDSA-AES256-SHA:DHE-RSA-AES128-SHA256:DHE-RSA-AES128-SHA:DHE-DSS-AES128-SHA256:DHE-RSA-AES256-SHA256:DHE-DSS-AES256-SHA:DHE-RSA-AES256-SHA:!aNULL:!eNULL:!EXPORT:!DES:!RC4:!3DES:!MD5:!PSK')

print("SUCCESS: Server listening on port", PORT)
https.serve_forever()