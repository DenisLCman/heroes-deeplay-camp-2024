module io.deeplay.camp.botfarm {
    exports io.deeplay.camp.botfarm.bots.denis_bots;

  requires java.desktop;
  requires io.deeplay.camp.game;
  requires org.slf4j;
  requires static lombok;
    requires com.fasterxml.jackson.databind;

  opens io.deeplay.camp.botfarm.bots.denis_bots to com.fasterxml.jackson.databind;

}
