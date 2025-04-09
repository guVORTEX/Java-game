import java.awt.Color;
import javalib.funworld.WorldScene;
import javalib.worldimages.TextImage;

interface ILoWord {
  WorldScene draw(WorldScene ws);
  
  ILoWord moveDown();
  
  boolean bottom();
  
  ILoWord typed(String key);
  
  int countWords();
  
  ILoWord append(Word word);
}

class MtLoWord implements ILoWord {
  /*
   * methods:
   * ... draw(WorldScene) ...                 --WorldScene
   * ... moveDown() ...                       --ILoWord
   * ... bottom() ...                         --boolean
   * ... typed(String) ...                    --ILoWord
   * ... countWords() ...                     --int
   * ... append(Word) ...                     --ILoWord
   */
  
  MtLoWord(){}
  
  //make no change when it is the end of the list
  public WorldScene draw(WorldScene ws) {
    return ws;
  }

  //make no change when it is the end of the list 
  public ILoWord moveDown() {
    return this;
  }

  //empty list will not reach bottom, thus return false
  public boolean bottom() {
    return false;
  }

  //make no change when it is the end of the list
  public ILoWord typed(String key) {
    return this;
  }

  //empty list, thus return 0
  public int countWords() {
    return 0;  
  }
  
  //add new word
  public ILoWord append(Word word) {
    return new ConsLoWord(word, this);
  }
}

class ConsLoWord implements ILoWord {
  /*
   * fields:
   * ... this.first ...                       --Word
   * ... this.rest ...                        --ILoWord
   * 
   * methods:
   * ... draw(WorldScene) ...                 --WorldScene
   * ... moveDown() ...                       --ILoWord
   * ... bottom() ...                         --boolean
   * ... typed(String) ...                    --ILoWord
   * ... countWords() ...                     --int
   * ... append(Word) ...                     --ILoWord
   * 
   * methods on fields:
   * ... this.rest.draw(WorldScene) ...       --WorldScene
   * ... this.first.moveDown() ...            --Word
   * ... this.rest.moveDown() ...             --ILoWord
   * ... this.first.bottom() ...              --boolean
   * ... this.rest.bottom() ...               --boolean
   * ... this.first.typed(ILoWord, String) ...--ILoWord
   * ... this.rest.countWords() ...           --int
   * ... this.rest.append(Word) ...           --ILoWord
   */
  
  Word first;
  ILoWord rest;

  ConsLoWord(Word first, ILoWord rest) {
    this.first = first;
    this.rest = rest;
  }
  
  //add all words in list to the scene
  public WorldScene draw(WorldScene acc) {
    return this.rest.draw(this.first.draw(acc));
  }

  //move all words in list down
  public ILoWord moveDown() {
    return new ConsLoWord(this.first.moveDown(), this.rest.moveDown());
  }

  //monitor if any of words in list reach bottom
  public boolean bottom() {
    return this.first.bottom() || this.rest.bottom();
  }

  //call typed in Word class
  public ILoWord typed(String key) {
    return this.first.typed(this.rest, key);
  }

  //calculate number of words in list
  public int countWords() {
    return 1 + this.rest.countWords(); 
  }
  
  //add new word to the end of list
  public ILoWord append(Word word) {
    this.rest = this.rest.append(word);
    return this;
  }
}


class Word {
  /*
   * fields:
   * ... this.word ...                        --String
   * ... this.x ...                           --int
   * ... this.y ...                           --int
   * 
   * methods:
   * ... draw(WorldScene) ...                 --WorldScene
   * ... moveDown() ...                       --Word
   * ... bottom() ...                         --boolean
   * ... typed(ILoWord, String) ...           --ILoWord
   */
  String word;
  int x;
  int y;

  Word(String word, int x, int y) {
    this.word = word;
    this.x = x;
    this.y = y;
  }

  //place word on scene
  public WorldScene draw(WorldScene scene) {
    return scene.placeImageXY(
        new TextImage(this.word, 20, Color.black), this.x, this.y);
  }
  
  //move word down
  public Word moveDown() {
    return new Word(this.word, this.x, this.y + 5);
  }
  
  //monitor if the word reaches bottom
  public boolean bottom() {
    return this.y >= 395;
  }
  
  //delete character from front of one word if that key is typed
  public ILoWord typed(ILoWord r, String key) {
    if ((this.word).startsWith(key)) {
      if (key.equals(this.word)) {
        return r;
      } 
      else {
        String remainingContent = this.word.substring(key.length());
        Word updatedWord = new Word(remainingContent, this.x, this.y);
        return new ConsLoWord(updatedWord, r);
      }
    } 
    else {
      return new ConsLoWord(this, r.typed(key));
    }
  }
}
