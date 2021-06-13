# babashka-scittle-guestbook

[Luminus](https://pragprog.com/titles/dswdcloj3/web-development-with-clojure-third-edition/) guestbook example for [babashka](https://github.com/babashka/babashka) + [scittle](https://github.com/borkdude/scittle)

With babashka installed, run `bb guestbook.clj` and a minimal Clojure web-abb opens up in your browser. Most useful for local applications and learning purposes (i.e. is a good [avocado project](https://lambdaisland.com/blog/2021-06-04-clojure-beginners-just-like-vegans-searching-for-good-cheese)). It is not intended for use in production.

## Helpful comments from experts

Reddit user [armincerf](https://www.reddit.com/r/Clojure/comments/nyf13s/luminus_guestbook_example_for_babashka_scittle/h1k9ep0?utm_source=share&utm_medium=web2x&context=3) said, you can run 
 ```
 fswatch -o . | xargs -n1 -I {} osascript -e 'tell application "Google Chrome" to tell the active tab of its first window to reload'
 ```
in a separate terminal to refresh the page whenever you change a file. Doesn't work for changing the clj though. 

Babashka creator borkdude [explained](https://www.reddit.com/r/Clojure/comments/nyf13s/luminus_guestbook_example_for_babashka_scittle/h1lq2qc?utm_source=share&utm_medium=web2x&context=3): The example runs a web server using babashka which is a scripting environment for Clojure with instant startup. The HTML contains a script tag with a reference to a ClojureScript file, which is evaluated by sci, a Clojure interpreter. Babashka uses the same Clojure interpreter, but compiled on the JVM with GraalVM native-image. The project which integrates sci with script tags is called scittle. To reload server code you can use the babashka nrepl or socket REPL. To reload CLJS you can also open a developer console and call `scittle.core.eval_script_tags()` manually.
