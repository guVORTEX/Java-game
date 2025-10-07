import javalib.funworld.*;
import javalib.worldimages.*;
import tester.Tester;
import java.awt.Color;
import java.util.Random;

class ZTypeWorld extends World {
  /*
   * fields:
   * ... this.words ...                   --ILoWord
   * ... this.score ...                   --int
   * ... this.rand ...                    --Random
   * 
   * methods:
   * ... makeScene() ...                  --WorldScene
   * ... onTick() ...                     --World
   * ... onKeyEvent(String) ...           --World
   * ... lastScene(String) ...            --WorldScene
   * 
   * methods one this field:
   * ... this.words.draw(WorldScene) ...  --WorldScene
   * ... this.words.moveDown() ...        --ILoWord
   * ... this.words.append(Word) ...      --ILoWord
   */
  
  ILoWord words;
  int score;
  Random rand;

  ZTypeWorld(ILoWord words, int score, Random rand) {
    this.words = words;
    this.score = score;
    this.rand = rand;
  }

  //make a game scene with score on it
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(600, 400);
    scene = this.words.draw(scene);
    scene = scene.placeImageXY(new TextImage("Score: " + this.score, 20, Color.BLACK), 550, 20);
    return scene;
  }
  
  //allow words move down, adding new words in the end of list, 
  //and monitor if word reaches the bottom as time passes
  public World onTick() {
    this.words = this.words.moveDown();

    if (this.rand.nextInt(10) < 2) {
      String newWord = this.randomWord(this.rand);
      Word word = new Word(newWord, this.rand.nextInt(560) + 20, 20);
      this.words = this.words.append(word);
    }

    if (this.words.bottom() || this.score >= 100) {
      return this.endOfWorld("Score: " + this.score);
    }
    return this;
  }
  
  //update word when key is typed, add score, and monitor if player wins
  public World onKeyEvent(String key) {
    int initialSize = this.words.countWords(); 
    this.words = this.words.typed(key);
    int newSize = this.words.countWords();
    
    if (initialSize > newSize) {
      this.score += 5;  
    }

    if (this.score >= 100) {
      return this.endOfWorld("You Win!");
    }
    return this;
  }
  
  //make scene when game ends
  public WorldScene lastScene(String msg) {
    return makeScene().placeImageXY(new TextImage(msg, 30, Color.red), 300, 200);
  }
  
  //create word with six random characters
  public String randomWord(Random rand) {
    String letters = "abcdefghijklmnopqrstuvwxyz";
    StringBuilder word = new StringBuilder();
    word.append(letters.charAt(rand.nextInt(letters.length())));
    word.append(letters.charAt(rand.nextInt(letters.length())));
    word.append(letters.charAt(rand.nextInt(letters.length())));
    word.append(letters.charAt(rand.nextInt(letters.length())));
    word.append(letters.charAt(rand.nextInt(letters.length())));
    word.append(letters.charAt(rand.nextInt(letters.length())));
    return word.toString();
  }
}

class ExamplesZType {
  Random rand = new Random(20); 
  ILoWord initialWords = new MtLoWord(); 
  ZTypeWorld initialWorld = new ZTypeWorld(initialWords, 0, rand);

  
  void testBigBang(Tester t) {
    int worldWidth = 600;
    int worldHeight = 400;
    double tickRate = 0.7; 
    initialWorld.bigBang(worldWidth, worldHeight, tickRate);
  }


  boolean testInitialScene(Tester t) {
    WorldScene emptyScene = new WorldScene(600, 400);
    emptyScene = emptyScene.placeImageXY(new TextImage("Score: 0", 20, Color.BLACK), 550, 20);
    return t.checkExpect(initialWorld.makeScene(), emptyScene);
  }


  boolean testGenerateRandomWord(Tester t) {
    String word1 = new String(new ZTypeWorld(this.initialWords,0, 
                                             this.rand).randomWord(new Random(1)));
    String word2 = new String(new ZTypeWorld(this.initialWords,0, 
                                             this.rand).randomWord(new Random(1)));
    return t.checkExpect(word1, word2);
  }
}