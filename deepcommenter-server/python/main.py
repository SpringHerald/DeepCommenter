from flask import Flask
app = Flask(__name__)
s1 = "a"


@app.route('/')
def hello_world():
    print(s1)
    return 'Hello World!'


@app.route('/s/<s>')
def f1(s):
    global s1
    print(s1)
    return 'hello %s' % s


def prepare():
    global s1
    s1 = 'aaa'
    pass


if __name__ == '__main__':
    prepare()
    app.run(host='0.0.0.0')
    print(s1)
