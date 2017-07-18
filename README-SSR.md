# Server-Side Rendering

This template includes server-side rendering, of which
the basics are covered in the
[Developer's Guide](https://fulcrologic.github.io/fulcro/guide.html#!/fulcro_devguide.M50_Server_Side_Rendering).

There are also extensive comments in the code. See:

- root.cljc for details about initial state tweaks
- server.clj for details on setting up state and rendering the pages on the server
- client.clj for details on starting the client with the server-generated state
- html5_routing.cljc for details on page routing

If you do not want server-side rendering, you simply:

1. Move the initial app state function back to InitialAppState on root
2. Remove the SSR component from the server (and the three or
so functions that do the rendering in that file)
3. Make an index.html file in resource/public
