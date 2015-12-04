EXAMPLE_PROJECT=example-project
EXAMPLE_PROJECT_PATH=example_project

cd ~/projects/untangled/template
rm -r $EXAMPLE_PROJECT

lein install &&
    lein new untangled $EXAMPLE_PROJECT -- :all &&
    cd $EXAMPLE_PROJECT &&
    lein repl
