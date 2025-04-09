import tester.*;
import javalib.impworld.*;
import javalib.worldimages.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

// World class of connection game
class ConnectionsWorld extends World {
  int width;
  int height;
  int rows;
  int cols;
  int cellSize;
  ArrayList<Word> words;
  ArrayList<Word> selectedWords;
  int attempts;
  Random rand;
  /*
   * Fields:
   * ... this.width ...           -- int
   * ... this.height ...          -- int
   * ... this.rows ...            -- int
   * ... this.cols ...            -- int
   * ... this.cellSize ...        -- int
   * ... this.words ...           -- ArrayList<Word>
   * ... this.selectedWords ...   -- ArrayList<Word>
   * ... this.attempts ...        -- int
   * ... this.rand ...            -- Random
   *
   * Methods:
   * ... this.makeScene() ...                       -- WorldScene
   * ... this.onMouseClicked(Posn) ...              -- void
   * ... this.onKeyEvent(String) ...                -- void
   * ... this.generateWords() ...                   -- ArrayList<Word>
   * ... this.shuffleWords() ...                    -- void
   * ... this.isSameCategory(List<Word>) ...        -- boolean
   * ... this.allGrouped() ...                      -- boolean
   *
   * Methods on Fields:
   * ... this.words.get(i) ...                      -- Word
   * ... this.rand.nextInt(n) ...                   -- int
   * ... this.selectedWords.add(w) ...              -- void
   * ... this.selectedWords.remove(w) ...           -- void
   */

  ConnectionsWorld(Random rand) {
    this.width = 800;
    this.height = 600;
    this.rows = 4;
    this.cols = 4;
    this.cellSize = 150;
    this.rand = rand;
    this.words = this.generateWords();
    this.selectedWords = new ArrayList<Word>();
    this.attempts = 0;
  }


  // Generate one of multiple word sets randomly
  ArrayList<Word> generateWords() {
    ArrayList<ArrayList<Word>> wordSets = new ArrayList<>(Arrays.asList(
        // Set 1: Color, Fruit, Animal, Vehicle
        new ArrayList<>(Arrays.asList(
        new Word("Red", "Color"), new Word("Blue", "Color"),
        new Word("Green", "Color"), new Word("Yellow", "Color"),

        new Word("Apple", "Fruit"), new Word("Orange", "Fruit"),
        new Word("Grape", "Fruit"), new Word("Lime", "Fruit"),

        new Word("Cat", "Animal"), new Word("Dog", "Animal"),
        new Word("Fox", "Animal"), new Word("Bear", "Animal"),

        new Word("Car", "Vehicle"), new Word("Bus", "Vehicle"),
        new Word("Bike", "Vehicle"), new Word("Train", "Vehicle")
      )),

        // Set 2: Season, Weather, Plant, Space
        new ArrayList<>(Arrays.asList(
        new Word("Spring", "Season"), new Word("Summer", "Season"),
        new Word("Fall", "Season"), new Word("Winter", "Season"),

        new Word("Thunder", "Weather"), new Word("Frost", "Weather"),
        new Word("Breeze", "Weather"), new Word("Snow", "Weather"),

        new Word("Tree", "Plant"), new Word("Leaf", "Plant"),
        new Word("Flower", "Plant"), new Word("Seed", "Plant"),

        new Word("Sun", "Space"), new Word("Moon", "Space"),
        new Word("Star", "Space"), new Word("Sky", "Space")
      )),

        // Set 3: Emotion, Weather, Time, Music
        new ArrayList<>(Arrays.asList(
        new Word("Happy", "Emotion"), new Word("Sad", "Emotion"),
        new Word("Angry", "Emotion"), new Word("Calm", "Emotion"),

        new Word("Windy", "Weather"), new Word("Cloudy", "Weather"),
        new Word("Stormy", "Weather"), new Word("Sunny", "Weather"),

        new Word("Morning", "Time"), new Word("Noon", "Time"),
        new Word("Evening", "Time"), new Word("Night", "Time"),

        new Word("Jazz", "Music"), new Word("Rock", "Music"),
        new Word("Pop", "Music"), new Word("Blues", "Music")
      )),

        // Set 4: Job, Tool, Shape, Insect
        new ArrayList<>(Arrays.asList(
        new Word("Doctor", "Job"), new Word("Teacher", "Job"),
        new Word("Engineer", "Job"), new Word("Artist", "Job"),

        new Word("Hammer", "Tool"), new Word("Wrench", "Tool"),
        new Word("Screwdriver", "Tool"), new Word("Saw", "Tool"),

        new Word("Circle", "Shape"), new Word("Square", "Shape"),
        new Word("Triangle", "Shape"), new Word("Hexagon", "Shape"),

        new Word("Ant", "Insect"), new Word("Bee", "Insect"),
        new Word("Fly", "Insect"), new Word("Moth", "Insect")
      )),

        // Set 5: Country, Capital, Currency, Landmark
        new ArrayList<>(Arrays.asList(
        new Word("USA", "Country"), new Word("France", "Country"),
        new Word("Japan", "Country"), new Word("Brazil", "Country"),

        new Word("Paris", "Capital"), new Word("Tokyo", "Capital"),
        new Word("Brasilia", "Capital"), new Word("Washington", "Capital"),

        new Word("Dollar", "Currency"), new Word("Yen", "Currency"),
        new Word("Euro", "Currency"), new Word("Real", "Currency"),

        new Word("Eiffel", "Landmark"), new Word("Statue", "Landmark"),
        new Word("Christ", "Landmark"), new Word("Tower", "Landmark")
      ))
    ));

    ArrayList<Word> chosen = new ArrayList<>(wordSets.get(this.rand.nextInt(wordSets.size())));

    // Randomly change words' order
    for (int i = chosen.size() - 1; i > 0; i--) {
      int j = this.rand.nextInt(i + 1);
      Word temp = chosen.get(i);
      chosen.set(i, chosen.get(j));
      chosen.set(j, temp);
    }

    return chosen;
  }


  // Draw the scene
  public WorldScene makeScene() {
    // Draw the grid of words
    WorldScene scene = new WorldScene(this.width, this.height);
    for (int i = 0; i < this.words.size(); i++) {
      int row = i / this.cols;
      int col = i % this.cols;
      int x = col * this.cellSize + this.cellSize / 2;
      int y = row * this.cellSize + this.cellSize / 2;
      Word w = this.words.get(i);

      WorldImage box = this.getBoxColor(w);
      WorldImage text = new TextImage(w.text, 14, Color.BLACK);
      WorldImage wordBox = new OverlayImage(text, box);

      scene.placeImageXY(wordBox, x, y);
    }
    
    // Shuffle button
    WorldImage shuffleBox = new RectangleImage(100, 30, "solid", Color.DARK_GRAY);
    WorldImage shuffleText = new TextImage("Shuffle", 16, Color.WHITE);
    WorldImage button = new OverlayImage(shuffleText, shuffleBox);
    scene.placeImageXY(button, this.width - 100, 550);
    
    // Deselect button
    WorldImage deselectBox = new RectangleImage(100, 30, "solid", Color.DARK_GRAY);
    WorldImage deselectText = new TextImage("Deselect", 16, Color.WHITE);
    WorldImage deselectButton = new OverlayImage(deselectText, deselectBox);
    scene.placeImageXY(deselectButton, this.width - 100, 500);

    // Display attempt count
    WorldImage attemptsText = new TextImage("Remain: ", 20, Color.BLACK);
    if (this.attempts <= 4) {
      attemptsText = new TextImage("Remain: " + (4 - this.attempts), 20, Color.BLACK);
    }
    else {
      attemptsText = new TextImage("Remain: 0", 20, Color.BLACK);
    }
    scene.placeImageXY(attemptsText, this.width - 100, 20);
    
    // Display win or lose
    if (this.attempts >= 4) {
      WorldImage gameOverText = new TextImage("Game Over", 36, Color.RED);
      scene.placeImageXY(gameOverText, this.width / 2, this.height - 50);
    }
    else if (this.allGrouped()) {
      WorldImage winText = new TextImage("You win with " + (1 + this.attempts) 
          + " tries!", 36, Color.MAGENTA);
      scene.placeImageXY(winText, this.width / 2, this.height - 50);
    }
    return scene;
  }

  // Draw box color based on word state
  WorldImage getBoxColor(Word w) {
    if (w.grouped) {
      return new RectangleImage(this.cellSize - 10, this.cellSize - 10, "solid", Color.GREEN);
    } 
    else if (w.selected) {
      return new RectangleImage(this.cellSize - 10, this.cellSize - 10, "solid", Color.YELLOW);
    } 
    else {
      return new RectangleImage(this.cellSize - 10, this.cellSize - 10, "solid", Color.LIGHT_GRAY);
    }
  }

  // Track mouse actions
  public void onMouseClicked(Posn pos) {
    int col = pos.x / this.cellSize;
    int row = pos.y / this.cellSize;
    int index = row * this.cols + col;
    
    // Detect Shuffle button click
    if (pos.x >= this.width - 150 
        && pos.x <= this.width - 50 
        && pos.y >= 535 
        && pos.y <= 565) {
      this.shuffleWords();
    }
    
    // Detect Deselect button click
    if (pos.x >= this.width - 150 
        && pos.x <= this.width - 50 
        && pos.y >= 485 
        && pos.y <= 515) {
      for (Word w : this.selectedWords) {
        w.selected = false;
      }
      this.selectedWords.clear();
    }

    // Detect word bars
    if (index >= 0 && index < this.words.size()) {
      Word w = this.words.get(index);

      if (!w.grouped) {
        if (w.selected) {
          w.selected = false;
          this.selectedWords.remove(w);
        } else if (this.selectedWords.size() < 4) {
          w.selected = true;
          this.selectedWords.add(w);
        }
      }

      if (this.selectedWords.size() == 4) {
        if (this.isSameCategory(this.selectedWords)) {
          for (Word word : this.selectedWords) {
            word.grouped = true;
            word.selected = false;
          }
        } else {
          this.attempts += 1;
          for (Word word : this.selectedWords) {
            word.selected = false;
          }
        }
        this.selectedWords.clear();
      }
    }
  }
  
  // Track key actions
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      this.words = this.generateWords();
      this.selectedWords = new ArrayList<Word>();
      this.attempts = 0;
    }
  }
  
  // To see if four words selected are the same type
  boolean isSameCategory(ArrayList<Word> selected) {
    if (selected.size() != 4) {
      return false;
    }
    String cat = selected.get(0).category;
    for (Word w : selected) {
      if (!w.category.equals(cat)) {
        return false;
      }
    }
    return true;
  }

  // Check if all words are grouped
  boolean allGrouped() {
    for (Word w : this.words) {
      if (!w.grouped) {
        return false;
      }
    }
    return true;
  }
  
  // Shuffle the order of words
  void shuffleWords() {
    for (int i = this.words.size() - 1; i > 0; i--) {
      int j = this.rand.nextInt(i + 1);
      Word temp = this.words.get(i);
      this.words.set(i, this.words.get(j));
      this.words.set(j, temp);
    }
  }
}

// Word class to clarify words' content and category
class Word {
  String text;
  String category;
  boolean selected;
  boolean grouped;

  /*
   * Fields:
   * ... this.text ...       -- String
   * ... this.category ...   -- String
   * ... this.selected ...   -- boolean
   * ... this.grouped ...    -- boolean
   */

  Word(String text, String category) {
    this.text = text;
    this.category = category;
    this.selected = false;
    this.grouped = false;
  }
}


// Example class
class ExamplesConnections {
  void testGame(Tester t) {
    Random r = new Random();
    ConnectionsWorld w = new ConnectionsWorld(r);
    w.bigBang(w.width, w.height, 0.1);
  }
  
  Word red = new Word("Red", "Color");
  Word blue = new Word("Blue", "Color");
  Word cat = new Word("Cat", "Animal");
  Word car = new Word("Car", "Vehicle");

  Random seededRand = new Random(123); 
  ConnectionsWorld cw = new ConnectionsWorld(seededRand);

  // Test constructor of Word
  void testWord(Tester t) {
    t.checkExpect(red.text, "Red");
    t.checkExpect(red.category, "Color");

    t.checkExpect(cat.text, "Cat");
    t.checkExpect(cat.category, "Animal");
  }

  // Test isSameCategory with same and mixed categories
  void testIsSameCategory(Tester t) {
    ArrayList<Word> allColor = new ArrayList<>(Arrays.asList(
        new Word("Red", "Color"),
        new Word("Blue", "Color"),
        new Word("Green", "Color"),
        new Word("Yellow", "Color")
    ));

    ArrayList<Word> mixed = new ArrayList<>(Arrays.asList(
        new Word("Red", "Color"),
        new Word("Blue", "Color"),
        new Word("Cat", "Animal"),
        new Word("Car", "Vehicle")
    ));

    t.checkExpect(cw.isSameCategory(allColor), true);
    t.checkExpect(cw.isSameCategory(mixed), false);
  }

  // Test that generateWords returns 16 words and they are randomized (seeded)
  void testGenerateWords(Tester t) {
    Random seededRand = new Random(123);
    ArrayList<Word> generated = new ConnectionsWorld(seededRand).words;
    t.checkExpect(generated.size(), 16);
    t.checkExpect(generated.get(0).text, "Blues");
    t.checkExpect(generated.get(0).category, "Music");
  }

  // Test makeScene
  void testMakeScene(Tester t) {
    // 1. Initial state
    ConnectionsWorld w1 = new ConnectionsWorld(new Random(123));
    WorldScene scene1 = w1.makeScene();
    t.checkExpect(scene1 != null, true);
    t.checkExpect(w1.attempts, 0);
    t.checkExpect(w1.allGrouped(), false);

    // 2. After partial correct grouping
    Word w2a = w1.words.get(0);
    Word w2b = w1.words.get(1);
    Word w2c = w1.words.get(2);
    Word w2d = w1.words.get(3);

    // Simulate correct group (same category)
    String groupCat = w2a.category;
    w2a.category = groupCat;
    w2b.category = groupCat;
    w2c.category = groupCat;
    w2d.category = groupCat;

    w2a.grouped = true;
    w2b.grouped = true;
    w2c.grouped = true;
    w2d.grouped = true;

    WorldScene scene2 = w1.makeScene();
    t.checkExpect(scene2 != null, true);
    t.checkExpect(w1.allGrouped(), false); // only 4 grouped

    // 3. Game over scenario
    ConnectionsWorld w3 = new ConnectionsWorld(new Random(2));
    w3.attempts = 4; // simulate loss
    WorldScene scene3 = w3.makeScene();
    t.checkExpect(scene3 != null, true);
    t.checkExpect(w3.attempts >= 4, true);
    t.checkExpect(w3.allGrouped(), false);

    // 4. Win scenario
    ConnectionsWorld w4 = new ConnectionsWorld(new Random(3));
    for (Word w : w4.words) {
      w.grouped = true;
    }
    WorldScene scene4 = w4.makeScene();
    t.checkExpect(scene4 != null, true);
    t.checkExpect(w4.allGrouped(), true);
  }
  
  // Test if allGrouped works correctly
  void testAllGrouped(Tester t) {
    // Set all words to grouped
    for (Word w : cw.words) {
      w.grouped = true;
    }
    t.checkExpect(cw.allGrouped(), true);
  
    // Unmark one word
    cw.words.get(0).grouped = false;
    t.checkExpect(cw.allGrouped(), false);
  }
  
  // Test Shuffle only change order
  void testShuffleWords(Tester t) {
    ArrayList<String> before = new ArrayList<>();
    for (Word w : cw.words) {
      before.add(w.text);
    }

    cw.shuffleWords();

    ArrayList<String> after = new ArrayList<>();
    for (Word w : cw.words) {
      after.add(w.text);
    }

    // Same contents regardless of order
    t.checkExpect(before.containsAll(after), true);
    t.checkExpect(after.containsAll(before), true);
  }

  // Test when r pressed
  void testOnKeyEventReset(Tester t) {
    cw.attempts = 2;
    cw.selectedWords.add(new Word("Temp", "Dummy"));
    cw.words.get(0).grouped = true;

    cw.onKeyEvent("r");

    t.checkExpect(cw.attempts, 0);
    t.checkExpect(cw.selectedWords.size(), 0);
    t.checkExpect(cw.words.get(0).grouped, false);
  }
  
  // Test functionality of clicking word bar
  void testMouseClickSelectDeselect(Tester t) {
    ConnectionsWorld w = new ConnectionsWorld(new Random(1));

    int x = 10;  
    int y = 10;
    Word first = w.words.get(0);

    w.onMouseClicked(new Posn(x, y));
    t.checkExpect(first.selected, true);
    t.checkExpect(w.selectedWords.contains(first), true);

    w.onMouseClicked(new Posn(x, y));
    t.checkExpect(first.selected, false);
    t.checkExpect(w.selectedWords.contains(first), false);
  }
  
  // Test functionality of clicking shuffle button
  void testShuffleButtonClick(Tester t) {
    ConnectionsWorld w = new ConnectionsWorld(new Random(123));

    ArrayList<String> before = new ArrayList<>();
    for (Word word : w.words) {
      before.add(word.text);
    }

    w.onMouseClicked(new Posn(700, 550)); 

    ArrayList<String> after = new ArrayList<>();
    for (Word word : w.words) {
      after.add(word.text);
    }

    boolean differentOrder = false;
    for (int i = 0; i < before.size(); i++) {
      if (!before.get(i).equals(after.get(i))) {
        differentOrder = true;
      }
    }

    t.checkExpect(before.containsAll(after), true);
    t.checkExpect(after.containsAll(before), true); 
    t.checkExpect(differentOrder, true);          
  }

  // Test functionality of deselect button
  void testDeselectButtonClick(Tester t) {
    ConnectionsWorld w = new ConnectionsWorld(new Random(123));

    Word w1 = w.words.get(0);
    Word w2 = w.words.get(1);
    w1.selected = true;
    w2.selected = true;
    w.selectedWords.add(w1);
    w.selectedWords.add(w2);

    w.onMouseClicked(new Posn(700, 500));

    t.checkExpect(w1.selected, false);
    t.checkExpect(w2.selected, false);
    t.checkExpect(w.selectedWords.size(), 0);
  }
}
