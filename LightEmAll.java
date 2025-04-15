import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// Main World class
class LightEmAll extends World {
  // Double array of game pieces
  ArrayList<ArrayList<GamePiece>> board;
  // A list of all nodes
  ArrayList<GamePiece> nodes;
  // A list of edges of the minimum spanning tree
  ArrayList<Edge> mst;
  // The game piece which sends power
  GamePiece powerStation;
  // Function as their name
  int width;
  int height;
  int powerRow;
  int powerCol;
  int radius;
  int rows;
  int cols;
  int steps;
  Random rand;

  /*
   * FIELDS:
   * ... this.board ...          -- ArrayList<ArrayList<GamePiece>>
   * ... this.nodes ...          -- ArrayList<GamePiece>
   * ... this.mst ...            -- ArrayList<Edge>
   * ... this.powerStation ...   -- GamePiece
   * ... this.width ...          -- int
   * ... this.height ...         -- int
   * ... this.powerRow ...       -- int
   * ... this.powerCol ...       -- int
   * ... this.radius ...         -- int
   * ... this.rows ...           -- int
   * ... this.cols ...           -- int
   * ... this.steps ...          -- int
   * ... this.rand ...           -- Random
   * 
   * METHODS:
   * ... this.makeBoard(int, int) ...                             -- ArrayList<ArrayList<GamePiece>>
   * ... this.randInit(ArrayList<ArrayList<GamePiece>>) ...       -- ArrayList<ArrayList<GamePiece>>
   * ... this.generateBoardKruskal() ...                          -- void
   * ... this.connectPieces(GamePiece, GamePiece) ...             -- void
   * ... this.computeDiameter() ...                               -- int
   * ... this.bfsFarthest(GamePiece) ...                          -- GamePiece
   * ... this.restart() ...                                       -- void
   * ... this.makeScene() ...                                     -- WorldScene
   * ... this.lastScene(String) ...                               -- WorldScene
   * ... this.onMouseClicked(Posn) ...                            -- void
   * ... this.onKeyEvent(String) ...                              -- void
   * ... this.powerUp() ...                                       -- void
   * ... this.powerFrom(GamePiece, int) ...                       -- void
   * ... this.allPowered() ...                                    -- boolean
   * 
   * METHODS ON FIELDS:
   * ... this.board.get(i).get(j) ...                             -- GamePiece
   * ... this.rand.nextInt(n) ...                                 -- int
   * ... this.powerStation.rotate() ...                           -- void
   */

  
  // Normally use for playing (to be completed)
  LightEmAll(int rows, int cols, Random r) {
    this.rows = rows;
    this.cols = cols;
    this.width = cols * 100;
    this.height = rows * 100;
    this.rand = r;
    this.board = this.makeBoard(rows, cols);
    this.generateBoardKruskal();       // connect the board using MST
    this.radius = (this.computeDiameter() / 2) + 1;
    this.randInit(this.board);         // spin the tiles
    this.powerStation = this.board.get(0).get(0);
    this.powerUp();
    this.steps = 0;
  }
  
  //For test
  LightEmAll(ArrayList<ArrayList<GamePiece>> board) {
    this.rows = board.size();
    this.cols = board.get(0).size();
    this.width = cols * 100;
    this.height = rows * 100;
    this.rand = new Random(15);
    this.board = board;
    this.nodes = new ArrayList<>();
    for (ArrayList<GamePiece> col : board) {
      this.nodes.addAll(col);
    }
    this.radius = (this.computeDiameter() / 2) + 1;
    this.powerStation = this.board.get(0).get(0);
    this.steps = 0;
    this.powerUp();
  }


  // Initialize board with given width and length
  ArrayList<ArrayList<GamePiece>> makeBoard(int rows, int cols) {
    ArrayList<ArrayList<GamePiece>> b = new ArrayList<>();
    for (int col = 0; col < cols; col++) {
      ArrayList<GamePiece> c = new ArrayList<>();
      for (int row = 0; row < rows; row++) {
        c.add(new GamePiece(row, col, false, false, false, false));
      }
      b.add(c);
    }
    return b;
  }
  
  // Generate random board
  void generateBoardKruskal() {
    this.nodes = new ArrayList<>();
    this.mst = new ArrayList<>();
    ArrayList<Edge> edges = new ArrayList<>();

    // Flatten board to node list
    for (ArrayList<GamePiece> col : board) {
      this.nodes.addAll(col);
    }

    // Create edges (right and bottom neighbors only)
    for (GamePiece gp : nodes) {
      int r = gp.row;
      int c = gp.col;

      if (c < cols - 1) {
        GamePiece right = board.get(c + 1).get(r);
        edges.add(new Edge(gp, right, rand.nextInt(100)));
      }

      if (r < rows - 1) {
        GamePiece down = board.get(c).get(r + 1);
        edges.add(new Edge(gp, down, rand.nextInt(100)));
      }
    }

    // Sort edges by weight
    Collections.sort(edges, new EdgeWeightComparator());

    UnionFind uf = new UnionFind(nodes);

    // Kruskal's algorithm
    for (Edge e : edges) {
      if (!uf.connected(e.fromNode, e.toNode)) {
        uf.union(e.fromNode, e.toNode);
        mst.add(e);
        connectPieces(e.fromNode, e.toNode);
      }
    }
  }
  
  // Determine relative position and connect wires
  void connectPieces(GamePiece a, GamePiece b) {
    if (a.row == b.row) {
      if (a.col < b.col) {
        a.right = true;
        b.left = true;
      } else {
        a.left = true;
        b.right = true;
      }
    } else if (a.col == b.col) {
      if (a.row < b.row) {
        a.bottom = true;
        b.top = true;
      } else {
        a.top = true;
        b.bottom = true;
      }
    }
  }

  // Restart the game when it's not end
  void restart() {
    this.board = this.makeBoard(rows, cols);
    this.generateBoardKruskal();
    this.randInit(board);
    this.powerStation = this.board.get(0).get(0);
    this.steps = 0;
    this.powerUp();
  }
  
  // Randomly rotate each tile in the board
  ArrayList<ArrayList<GamePiece>> randInit(ArrayList<ArrayList<GamePiece>> board) {
    Random rand = this.rand;
    for (ArrayList<GamePiece> col : board) {
      for (GamePiece gp : col) {
        int rotations = rand.nextInt(4); // 0 to 3 times
        for (int i = 0; i < rotations; i++) {
          gp.rotate();
        }
      }  
    }
    return board;
  }

  // Rendering the game
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(this.width, this.height);
    for (int col = 0; col < this.cols; col++) {
      for (int row = 0; row < this.rows; row++) {
        GamePiece gp = this.board.get(col).get(row);
        boolean isPowerStation = gp == this.powerStation;
        WorldImage img = gp.tileImage(100, 20, 
            gp.colorSelection(), isPowerStation);
        scene.placeImageXY(img, col * 100 + 50, row * 100 + 50);
        
        WorldImage sqr = new RectangleImage(100, 100, OutlineMode.OUTLINE, Color.BLACK);
        scene.placeImageXY(sqr, col * 100 + 50, row * 100 + 50);
      }
    }
    WorldImage score = new TextImage("Steps: " + this.steps, 18, Color.WHITE);
    scene.placeImageXY(score, 50, 15);
    return scene;
  }
  
  // Show msg when win
  public WorldScene lastScene(String msg) {
    WorldScene scene = this.makeScene();
    WorldImage message = new TextImage(msg, 6 * this.cols, FontStyle.BOLD, Color.GREEN);
    scene.placeImageXY(message, this.width / 2, this.height / 2);
    return scene;
  }

  // Rotate when mouse clicks
  public void onMouseClicked(Posn pos) {
    int col = pos.x / 100;
    int row = pos.y / 100;
    if (col < this.cols && row < this.rows) {
      GamePiece gp = this.board.get(col).get(row);
      gp.rotate();
      this.steps++;
      this.powerUp();
    }
  }

  // Move power station and restart when keys pressed
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      this.restart();
    }
    
    if (key.equals("right") && this.powerStation.col + 1 < cols) {
      this.powerStation = board.get(this.powerStation.col + 1).get(this.powerStation.row);
      this.steps++;
    }
    if (key.equals("left") && this.powerStation.col - 1 >= 0) {
      this.powerStation = board.get(this.powerStation.col - 1).get(this.powerStation.row);
      this.steps++;
    }
    if (key.equals("down") && this.powerStation.row + 1 < rows) {
      this.powerStation = board.get(this.powerStation.col).get(this.powerStation.row + 1);
      this.steps++;
    }
    if (key.equals("up") && this.powerStation.row - 1 >= 0) {
      this.powerStation = board.get(this.powerStation.col).get(this.powerStation.row - 1);
      this.steps++;
    }
    this.powerUp();
  }
  
  // If all tiles have power
  boolean allPowered() {
    for (ArrayList<GamePiece> col : this.board) {
      for (GamePiece gp : col) {
        if (!gp.powered) {
          return false;
        }
      }
    }
    return true;
  }

  // Reset power and spread power again
  void powerUp() {
    for (ArrayList<GamePiece> col : this.board) {
      for (GamePiece gp : col) {
        gp.powered = false;
        gp.powerLevel = -1;
      }
    }
    this.powerFrom(this.powerStation, 0);

    if (this.allPowered()) {
      this.radius = (this.computeDiameter() / 2) + 1; // recalculate on full connection
      this.endOfWorld("All tiles are powered! You win!");
    }
  }

  // Spread power
  void powerFrom(GamePiece start, int level) {
    ArrayList<GamePiece> worklist = new ArrayList<>();
    start.powerLevel = level;
    worklist.add(start);

    while (!worklist.isEmpty()) {
      GamePiece current = worklist.remove(0);
      if (!current.powered && current.powerLevel <= this.radius) {
        current.powered = true;

        int r = current.row;
        int c = current.col;
        int nextLevel = current.powerLevel + 1;

        if (current.top && r > 0) {
          GamePiece above = this.board.get(c).get(r - 1);
          if (above.bottom && above.powerLevel == -1) {
            above.powerLevel = nextLevel;
            worklist.add(above);
          }
        }
        if (current.bottom && r < rows - 1) {
          GamePiece below = this.board.get(c).get(r + 1);
          if (below.top && below.powerLevel == -1) {
            below.powerLevel = nextLevel;
            worklist.add(below);
          }
        }
        if (current.left && c > 0) {
          GamePiece left = this.board.get(c - 1).get(r);
          if (left.right && left.powerLevel == -1) {
            left.powerLevel = nextLevel;
            worklist.add(left);
          }
        }
        if (current.right && c < cols - 1) {
          GamePiece right = this.board.get(c + 1).get(r);
          if (right.left && right.powerLevel == -1) {
            right.powerLevel = nextLevel;
            worklist.add(right);
          }
        }
      }
    }
  }
  
  // Use BFS to calculate reasonable radius
  int computeDiameter() {
    GamePiece start = this.board.get(0).get(0);
    GamePiece farthest1 = bfsFarthest(start);
    GamePiece farthest2 = bfsFarthest(farthest1);
    return farthest2.powerLevel;
  }
  
  // Find farthest with BFS
  GamePiece bfsFarthest(GamePiece start) {
    for (ArrayList<GamePiece> col : this.board) {
      for (GamePiece gp : col) {
        gp.powerLevel = -1;
      }
    }

    ArrayList<GamePiece> worklist = new ArrayList<>();
    start.powerLevel = 0;
    worklist.add(start);
    GamePiece farthest = start;

    while (!worklist.isEmpty()) {
      GamePiece current = worklist.remove(0);
      farthest = current;

      int r = current.row;
      int c = current.col;
      int nextLevel = current.powerLevel + 1;

      if (current.top && r > 0) {
        GamePiece above = this.board.get(c).get(r - 1);
        if (above.bottom && above.powerLevel == -1) {
          above.powerLevel = nextLevel;
          worklist.add(above);
        }
      }
      if (current.bottom && r < rows - 1) {
        GamePiece below = this.board.get(c).get(r + 1);
        if (below.top && below.powerLevel == -1) {
          below.powerLevel = nextLevel;
          worklist.add(below);
        }
      }
      if (current.left && c > 0) {
        GamePiece left = this.board.get(c - 1).get(r);
        if (left.right && left.powerLevel == -1) {
          left.powerLevel = nextLevel;
          worklist.add(left);
        }
      }
      if (current.right && c < cols - 1) {
        GamePiece right = this.board.get(c + 1).get(r);
        if (right.left && right.powerLevel == -1) {
          right.powerLevel = nextLevel;
          worklist.add(right);
        }
      }
    }

    return farthest;
  }
}

// Class represents power station and wires
class GamePiece {
  int row;
  int col;
  boolean left;
  boolean right;
  boolean top;
  boolean bottom;
  boolean powerStation;
  boolean powered;
  int powerLevel;
  
  /*
   * FIELDS:
   * ... this.row ...           -- int
   * ... this.col ...           -- int
   * ... this.left ...          -- boolean
   * ... this.right ...         -- boolean
   * ... this.top ...           -- boolean
   * ... this.bottom ...        -- boolean
   * ... this.powerStation ...  -- boolean
   * ... this.powered ...       -- boolean
   * ... this.powerLevel ...    -- int
   * 
   * METHODS:
   * ... this.rotate() ...                                        -- void
   * ... this.tileImage(int, int, Color, boolean) ...             -- WorldImage
   * ... this.colorSelection() ...                                -- Color
   */

  
  GamePiece(int row, int col, boolean top, boolean right, boolean bottom, boolean left) {
    this.row = row;
    this.col = col;
    this.top = top;
    this.right = right;
    this.bottom = bottom;
    this.left = left;
    this.powered = false;
    this.powerLevel = -1;
  }
  
  // Return color based on its power state
  public Color colorSelection() {
    if (!this.powered) {
      return Color.LIGHT_GRAY;
    }

    int max = 10; // max depth to normalize color fade
    int level = Math.min(this.powerLevel, max);

    int red = 255;
    int green = Math.max(0, 255 - level * 20);
    int blue = 0;

    return new Color(red, green, blue);
  }


  // Rotate 90 degrees clockwise
  void rotate() {
    boolean temp = this.top;
    this.top = this.left;
    this.left = this.bottom;
    this.bottom = this.right;
    this.right = temp;
  }
  
  //Start tile image off as a blue square with a wire-width square in the middle,
  // to make image "cleaner" (will look strange if tile has no wire, but that can't be)
  WorldImage tileImage(int size, int wireWidth, Color wireColor, boolean hasPowerStation) {
    WorldImage image = new OverlayImage(
        new RectangleImage(wireWidth, wireWidth, OutlineMode.SOLID, wireColor),
        new RectangleImage(size, size, OutlineMode.SOLID, Color.DARK_GRAY));
    WorldImage vWire = new RectangleImage(wireWidth, (size + 1) / 2, OutlineMode.SOLID, wireColor);
    WorldImage hWire = new RectangleImage((size + 1) / 2, wireWidth, OutlineMode.SOLID, wireColor);
 
    if (this.top) {
      image = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP, vWire, 0, 0, image);
    }
    if (this.right) {
      image = new OverlayOffsetAlign(AlignModeX.RIGHT, AlignModeY.MIDDLE, hWire, 0, 0, image);
    }
    if (this.bottom) {
      image = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM, vWire, 0, 0, image);
    }
    if (this.left) {
      image = new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.MIDDLE, hWire, 0, 0, image);
    }
    if (hasPowerStation) {
      image = new OverlayImage(
                  new OverlayImage(
                      new StarImage(size / 3, 7, OutlineMode.OUTLINE, new Color(255, 128, 0)),
                      new StarImage(size / 3, 7, OutlineMode.SOLID, new Color(0, 255, 255))),
                  image);
    }
    return image;
  }
}


// Represent routine from node to node
class Edge {
  GamePiece fromNode;
  GamePiece toNode;
  int weight;
  
  /*
   * FIELDS:
   * ... this.fromNode ...    -- GamePiece
   * ... this.toNode ...      -- GamePiece
   * ... this.weight ...      -- int
   */
  
  Edge(GamePiece from, GamePiece to, int weight) {
    this.fromNode = from;
    this.toNode = to;
    this.weight = weight;
  }
}

// Represent data structure to track connected components
class UnionFind {
  HashMap<GamePiece, GamePiece> parent;

  /*
   * FIELDS:
   * ... this.parent ...      -- HashMap<GamePiece, GamePiece>
   * 
   * METHODS:
   * ... this.find(GamePiece) ...                                 -- GamePiece
   * ... this.union(GamePiece, GamePiece) ...                     -- void
   * ... this.connected(GamePiece, GamePiece) ...                 -- boolean
   */
  
  UnionFind(ArrayList<GamePiece> nodes) {
    parent = new HashMap<>();
    for (GamePiece n : nodes) {
      parent.put(n, n);
    }
  }

  //Find the representative root of the set containing g
  GamePiece find(GamePiece g) {
    if (!parent.get(g).equals(g)) {
      parent.put(g, find(parent.get(g))); // path compression
    }
    return parent.get(g);
  }

  // Set the root of one as the parent of the other
  void union(GamePiece a, GamePiece b) {
    GamePiece rootA = find(a);
    GamePiece rootB = find(b);
    if (!rootA.equals(rootB)) {
      parent.put(rootA, rootB);
    }
  }

  // Returns if GamePieces a and b are in the same set
  boolean connected(GamePiece a, GamePiece b) {
    return find(a).equals(find(b));
  }
}

// Comparator class to compare weight
class EdgeWeightComparator implements Comparator<Edge> {
  public int compare(Edge e1, Edge e2) {
    return Integer.compare(e1.weight, e2.weight);
  }
}

// Example and Test class
class ExampleLight {
  Random r = new Random(15);

  GamePiece a00; 
  GamePiece a01; 
  GamePiece a10;
  ArrayList<ArrayList<GamePiece>> board;
  LightEmAll world;

  void initSimpleBoard() {
    a00 = new GamePiece(0, 0, false, true, false, false); 
    a01 = new GamePiece(0, 1, false, false, false, true); 
    a10 = new GamePiece(1, 0, false, false, false, false);
    board = new ArrayList<>();
    board.add(new ArrayList<>(Arrays.asList(a00, a10)));
    board.add(new ArrayList<>(Arrays.asList(a01, new GamePiece(1, 1, false, false, false, false))));

    world = new LightEmAll(board);
    world.powerStation = a00;
    world.powerUp();
  }

  void testRotate(Tester t) {
    GamePiece gp = new GamePiece(0, 0, true, false, false, false); // top wire
    gp.rotate();
    t.checkExpect(gp.right, true);
    t.checkExpect(gp.top, false);
  }

  void testMouseClickRotates(Tester t) {
    initSimpleBoard();
    t.checkExpect(a01.left, true);

    world.onMouseClicked(new Posn(150, 50));
    t.checkExpect(a01.top, true);
    t.checkExpect(a01.left, false);
  }

  void testKeyMovement(Tester t) {
    initSimpleBoard();
    t.checkExpect(world.powerStation, a00);

    world.onKeyEvent("right");
    t.checkExpect(world.powerStation, a01);

    world.onKeyEvent("left");
    t.checkExpect(world.powerStation, a00);
  }

  void testPowerUpClearsAll(Tester t) {
    initSimpleBoard();
    a10.powered = true;
    world.powerUp();
    t.checkExpect(a10.powered, false);
  }

  void testPowerFromSpreadsOnlyConnected(Tester t) {
    initSimpleBoard();
    t.checkExpect(a00.powered, true); 
    t.checkExpect(a01.powered, true); 
    t.checkExpect(a10.powered, false); 
  }

  void testColorSelectionGradient(Tester t) {
    GamePiece unpowered = new GamePiece(0, 0, false, false, false, false);
    unpowered.powered = false;
    t.checkExpect(unpowered.colorSelection(), Color.LIGHT_GRAY);

    GamePiece p0 = new GamePiece(0, 0, false, false, false, false);
    p0.powered = true;
    p0.powerLevel = 0;
    t.checkExpect(p0.colorSelection(), new Color(255, 255, 0));

    GamePiece p1 = new GamePiece(0, 0, false, false, false, false);
    p1.powered = true;
    p1.powerLevel = 1;
    t.checkExpect(p1.colorSelection(), new Color(255, 235, 0));

    GamePiece p5 = new GamePiece(0, 0, false, false, false, false);
    p5.powered = true;
    p5.powerLevel = 5;
    t.checkExpect(p5.colorSelection(), new Color(255, 155, 0));

    GamePiece p10 = new GamePiece(0, 0, false, false, false, false);
    p10.powered = true;
    p10.powerLevel = 10;
    t.checkExpect(p10.colorSelection(), new Color(255, 55, 0));

    GamePiece p15 = new GamePiece(0, 0, false, false, false, false);
    p15.powered = true;
    p15.powerLevel = 15;
    t.checkExpect(p15.colorSelection(), new Color(255, 55, 0));
  }


  void testRandInit(Tester t) {
    GamePiece g0 = new GamePiece(0, 0, true, false, false, false);
    GamePiece g1 = new GamePiece(1, 0, false, true, false, false);

    ArrayList<ArrayList<GamePiece>> testBoard = new ArrayList<>();
    testBoard.add(new ArrayList<>(Arrays.asList(g0, g1)));

    GamePiece g0Copy = new GamePiece(0, 0, true, false, false, false);
    GamePiece g1Copy = new GamePiece(1, 0, false, true, false, false);

    ArrayList<ArrayList<GamePiece>> originalBoard = new ArrayList<>();
    originalBoard.add(new ArrayList<>(Arrays.asList(g0Copy, g1Copy)));

    LightEmAll testWorld = new LightEmAll(testBoard);
    testWorld.randInit(testBoard); 

    boolean g0Same = g0.top == g0Copy.top && g0.right == g0Copy.right 
                  && g0.bottom == g0Copy.bottom && g0.left == g0Copy.left;

    boolean g1Same = g1.top == g1Copy.top && g1.right == g1Copy.right 
                  && g1.bottom == g1Copy.bottom && g1.left == g1Copy.left;

    t.checkExpect(g0Same || g1Same, false);
  }


  void testComputeDiameter(Tester t) {
    GamePiece a = new GamePiece(0, 0, false, true, false, false);
    GamePiece b = new GamePiece(0, 1, false, false, false, true);

    ArrayList<ArrayList<GamePiece>> board = new ArrayList<>();
    board.add(new ArrayList<>(Arrays.asList(a)));
    board.add(new ArrayList<>(Arrays.asList(b)));

    LightEmAll game = new LightEmAll(board);
    t.checkExpect(game.computeDiameter(), 0);
  }

  void testConnectPieces(Tester t) {
    GamePiece a = new GamePiece(0, 0, false, false, false, false);
    GamePiece b = new GamePiece(0, 1, false, false, false, false);

    ArrayList<ArrayList<GamePiece>> board = new ArrayList<>();
    board.add(new ArrayList<>(Arrays.asList(a)));
    board.add(new ArrayList<>(Arrays.asList(b)));

    LightEmAll game = new LightEmAll(board);
    game.connectPieces(a, b);
    t.checkExpect(a.right, true);
    t.checkExpect(b.left, true);
  }

  void testBfsFarthest(Tester t) {
    GamePiece a = new GamePiece(0, 0, false, true, false, false); 
    GamePiece b = new GamePiece(0, 1, false, false, false, true); 

    ArrayList<ArrayList<GamePiece>> board = new ArrayList<>();
    board.add(new ArrayList<>(Arrays.asList(a))); 
    board.add(new ArrayList<>(Arrays.asList(b))); 

    LightEmAll game = new LightEmAll(board);
    game.connectPieces(a, b);
    game.powerUp();

    GamePiece far = game.bfsFarthest(a);

    t.checkExpect(far.powerLevel, 0);
  }

  void testBigBangGame(Tester t) {
    LightEmAll game = new LightEmAll(8, 5, r);
    game.bigBang(game.width, game.height, 0.1);
  }

}
