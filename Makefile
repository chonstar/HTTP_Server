pkg = http
source = $(pkg)/HTTPRequest.java $(pkg)/HTTPResponse.java $(pkg)/HTTPConnection.java $(pkg)/HTTPServer.java
jc = javac

classfiles = $(source:.java=.class)

all: $(classfiles)

%.class: %.java
	$(jc) $<

clean:
	rm -f $(pkg)/*.class
