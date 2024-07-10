from flask import Flask, request, abort
import os

app = Flask(__name__)

@app.route("/save_screenshot/<language>/<name>", methods=["PUT"])
def save_screenshot(language, name):
    try:
        dir = f"fastlane/metadata/android/{language}/images/phoneScreenshots"
        os.makedirs(dir, exist_ok=True)

        with open(f"{dir}/{name}.png", "wb") as f:
            f.write(request.data)

        return f"File saved to {dir}/{name}.png", 200
    except Exception as e:
        abort(500, description=f"Internal Server Error")

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, ssl_context="adhoc")
