package dev.totallyspies.spydle.gameserver.game.words;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/*
   For some substrings it is harder to come up with a word than for others.
   For every combination of 2 and 3 letters, we count the number of words that contain this combination.
*/
public class SubstringGenerator {

  public static void main(String[] x) {
    var words = new SubstringGenerator();
    try {
      words.writeSubstrings();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /* Please don't call this function too often, the file that it reads is heavy and contains a lot of words. */
  public List<String> getWords() throws IOException {
    var words = new ArrayList<String>();
    File file = new File("words.txt");
    BufferedReader br = new BufferedReader(new FileReader(file));
    String line;
    while ((line = br.readLine()) != null) {
      if (!line.isEmpty()) {
        words.add(line.trim().toLowerCase());
      }
    }
    return words;
  }

  /* Writes to a file substrings.csv */
  public void writeSubstrings() throws IOException {
    int[][] occurrences2 = new int[26][26];
    int[][][] occurrences3 = new int[26][26][26];
    List<Substring> substrings = new ArrayList<>();

    var words = getWords();
    for (char c1 = 'a'; c1 <= 'z'; c1++) {
      for (char c2 = 'a'; c2 <= 'z'; c2++) {
        String s = new StringBuilder().append(c1).append(c2).toString();
        for (var word : words) {
          if (word.contains(s)) {
            occurrences2[c1 - 'a'][c2 - 'a']++;
          }
        }
        substrings.add(new Substring(s, occurrences2[c1 - 'a'][c2 - 'a']));
      }
    }

    for (char c1 = 'a'; c1 <= 'z'; c1++) {
      for (char c2 = 'a'; c2 <= 'z'; c2++) {
        for (char c3 = 'a'; c3 <= 'z'; c3++) {
          String s = new StringBuilder().append(c1).append(c2).append(c3).toString();
          for (var word : words) {
            if (word.contains(s)) {
              occurrences3[c1 - 'a'][c2 - 'a'][c3 - 'a']++;
            }
          }
          substrings.add(new Substring(s, occurrences3[c1 - 'a'][c2 - 'a'][c3 - 'a']));
        }
      }
    }

    substrings.sort(Comparator.comparing(Substring::occurrences));

    System.out.println("substring, occurrences");
    for (var x : substrings) {
      if (x.occurrences() > 0) {
        System.out.println(x.s() + "," + x.occurrences());
      }
    }
  }

  private record Substring(String s, int occurrences) {}
}
