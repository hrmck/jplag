package jplag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * This method represents the whole result of a comparison between two submissions.
 */
public class JPlagComparison implements Comparator<JPlagComparison> {

  public Submission subA;
  public Submission subB;

  public AllBasecodeMatches bcmatchesA = null;
  public AllBasecodeMatches bcmatchesB = null;

  public List<Match> matches = new ArrayList<>();

  public JPlagComparison(Submission subA, Submission subB) {
    super();
    this.subA = subA;
    this.subB = subB;
  }

  public final void addMatch(int startA, int startB, int length) {
    for (Match match : matches) {
      if (match.overlap(startA, startB, length)) {
        return;
      }
    }

    matches.add(new Match(startA, startB, length));
  }

  /* s==0 uses the start indexes of subA as key for the sorting algorithm.
   * Otherwise the start indexes of subB are used. */
  public final int[] sort_permutation(int s) {   // bubblesort!!!
    int size = matches.size();
    int[] perm = new int[size];
    int i, j, tmp;

    // initialize permutation array
    for (i = 0; i < size; i++) {
      perm[i] = i;
    }

    if (s == 0) {     // submission A
      for (i = 1; i < size; i++) {
        for (j = 0; j < (size - i); j++) {
          if (matches.get(perm[j]).startA > matches.get(perm[j + 1]).startA) {
            tmp = perm[j];
            perm[j] = perm[j + 1];
            perm[j + 1] = tmp;
          }
        }
      }
    } else {        // submission B
      for (i = 1; i < size; i++) {
        for (j = 0; j < (size - i); j++) {
          if (matches.get(perm[j]).startB > matches.get(perm[j + 1]).startB) {
            tmp = perm[j];
            perm[j] = perm[j + 1];
            perm[j + 1] = tmp;
          }
        }
      }
    }
    return perm;
  }

  /* sort start indexes of subA
   */
  public final void sort() {   // bubblesort!!!
    Match tmp;
    int size = matches.size();
    int i, j;

    for (i = 1; i < size; i++) {
      for (j = 0; j < (size - i); j++) {
        if (matches.get(j).startA > matches.get(j + 1).startA) {
          tmp = matches.get(j);
          matches.set(j, matches.get(j + 1));
          matches.set(j + 1, tmp);
        }
      }
    }
  }

  /* A few methods to calculate some statistical data
   */

  /**
   * Get the total number of matched tokens for this comparison.
   */
  public final int getNumberOfMatchedTokens() {
    int numberOfMatchedTokens = 0;

    for (Match match : matches) {
      numberOfMatchedTokens += match.length;
    }

    return numberOfMatchedTokens;
  }

  private int biggestMatch() {
    int erg = 0;

    for (Match match : matches) {
      if (match.length > erg) {
        erg = match.length;
      }
    }

    return erg;
  }

  public final boolean moreThan(float percent) {
    return (percent() > percent);
  }

  public final float roundedPercent() {
    float percent = percent();
    return ((int) (percent * 10)) / (float) 10;
  }

  public final float percent() {
    float sa, sb;
    if (bcmatchesB != null && bcmatchesA != null) {
      sa = subA.getNumberOfTokens() - subA.files.size() - bcmatchesA.getNumberOfMatchedTokens();
      sb = subB.getNumberOfTokens() - subB.files.size() - bcmatchesB.getNumberOfMatchedTokens();
    } else {
      sa = subA.getNumberOfTokens() - subA.files.size();
      sb = subB.getNumberOfTokens() - subB.files.size();
    }
    return (200 * (float) getNumberOfMatchedTokens()) / (sa + sb);
  }

  public final float percentA() {
    int divisor;
    if (bcmatchesA != null) {
      divisor = subA.getNumberOfTokens() - subA.files.size() - bcmatchesA.getNumberOfMatchedTokens();
    } else {
      divisor = subA.getNumberOfTokens() - subA.files.size();
    }
    return (divisor == 0 ? 0f : (getNumberOfMatchedTokens() * 100 / (float) divisor));
  }

  public final float percentB() {
    int divisor;
    if (bcmatchesB != null) {
      divisor = subB.getNumberOfTokens() - subB.files.size() - bcmatchesB.getNumberOfMatchedTokens();
    } else {
      divisor = subB.getNumberOfTokens() - subB.files.size();
    }
    return (divisor == 0 ? 0f : (getNumberOfMatchedTokens() * 100 / (float) divisor));
  }

  public final float roundedPercentMaxAB() {
    float percent = percentMaxAB();
    return ((int) (percent * 10)) / (float) 10;
  }

  public final float percentMaxAB() {
    float a = percentA();
    float b = percentB();
    if (a > b) {
      return a;
    } else {
      return b;
    }
  }

  public final float roundedPercentMinAB() {
    float percent = percentMinAB();
    return ((int) (percent * 10)) / (float) 10;
  }

  public final float percentMinAB() {
    float a = percentA();
    float b = percentB();
    if (a < b) {
      return a;
    } else {
      return b;
    }
  }

  public final float percentBasecodeA() {
    float sa = subA.getNumberOfTokens() - subA.files.size();
    return bcmatchesA.getNumberOfMatchedTokens() * 100 / sa;
  }

  public final float percentBasecodeB() {
    float sb = subB.getNumberOfTokens() - subB.files.size();
    return bcmatchesB.getNumberOfMatchedTokens() * 100 / sb;
  }

  public final float roundedPercentBasecodeA() {
    float percent = percentBasecodeA();
    return ((int) (percent * 10)) / (float) 10;
  }

  public final float roundedPercentBasecodeB() {
    float percent = percentBasecodeB();
    return ((int) (percent * 10)) / (float) 10;
  }

  /* This method returns all the files which contributed to a match.
   * Parameter: j == 0   submission A,
   *            j != 0   submission B.
   */
  public final String[] files(int j) {
    Token[] tokens = (j == 0 ? subA : subB).tokenList.tokens;
    int i, h, starti, starth, count = 1;
    o1:
    for (i = 1; i < matches.size(); i++) {
      starti = (j == 0 ? matches.get(i).startA : matches.get(i).startB);
      for (h = 0; h < i; h++) {
        starth = (j == 0 ? matches.get(h).startA : matches.get(h).startB);
        if (tokens[starti].file.equals(tokens[starth].file)) {
          continue o1;
        }
      }
      count++;
    }
    String[] res = new String[count];
    res[0] = tokens[(j == 0 ? matches.get(0).startA : matches.get(0).startB)].file;
    count = 1;
    o2:
    for (i = 1; i < matches.size(); i++) {
      starti = (j == 0 ? matches.get(i).startA : matches.get(i).startB);
      for (h = 0; h < i; h++) {
        starth = (j == 0 ? matches.get(h).startA : matches.get(h).startB);
        if (tokens[starti].file.equals(tokens[starth].file)) {
          continue o2;
        }
      }
      res[count++] = tokens[starti].file;
    }

    /* sort by file name. (so that equally named files are displayed
     * approximately side by side.) */
    Arrays.sort(res);

    return res;
  }

  /* This method returns the name of all files that are represented by
   * at least one token. */
  public final String[] allFiles(int sub) {
    Structure struct = (sub == 0 ? subA : subB).tokenList;
    int count = 1;
    for (int i = 1; i < struct.size(); i++) {
      if (!struct.tokens[i].file.equals(struct.tokens[i - 1].file)) {
        count++;
      }
    }
    String[] res = new String[count];
    if (count > 0) {
      res[0] = struct.tokens[0].file;
    }
    count = 1;
    for (int i = 1; i < struct.size(); i++) {
      if (!struct.tokens[i].file.equals(struct.tokens[i - 1].file)) {
        res[count++] = struct.tokens[i].file;
      }
    }

    /* bubblesort by file name. (so that equally named files are displayed
     * approximately side by side.) */
    for (int a = 1; a < res.length; a++) {
      for (int b = 1; b < (res.length - a); b++) {
        if (res[b - 1].compareTo(res[b]) < 0) {
          String hilf = res[b - 1];
          res[b - 1] = res[b];
          res[b] = hilf;
        }
      }
    }

    return res;
  }

  public int compare(JPlagComparison o1, JPlagComparison o2) {
    float p1 = o1.percent();
    float p2 = o2.percent();
    if (p1 == p2) {
      return 0;
    }
    if (p1 > p2) {
      return -1;
    } else {
      return 1;
    }
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof JPlagComparison)) {
      return false;
    }
    return (compare(this, (JPlagComparison) obj) == 0);
  }

  public String toString() {
    return subA.name + " <-> " + subB.name;
  }

  public static class AvgComparator implements Comparator<JPlagComparison> {

    public int compare(JPlagComparison o1, JPlagComparison o2) {
      float p1 = o1.percent();
      float p2 = o2.percent();
      if (p1 == p2) {
        return 0;
      }
      if (p1 > p2) {
        return -1;
      } else {
        return 1;
      }
    }
  }

  public static class AvgReversedComparator implements Comparator<JPlagComparison> {

    public int compare(JPlagComparison o1, JPlagComparison o2) {
      float p1 = o1.percent();
      float p2 = o2.percent();
      if (p1 == p2) {
        return 0;
      }
      if (p1 < p2) {
        return -1;
      } else {
        return 1;
      }
    }
  }

  public static class MaxComparator implements Comparator<JPlagComparison> {

    public int compare(JPlagComparison o1, JPlagComparison o2) {
      float p1 = o1.percentMaxAB();
      float p2 = o2.percentMaxAB();
      if (p1 == p2) {
        return 0;
      }
      if (p1 > p2) {
        return -1;
      } else {
        return 1;
      }
    }
  }

  public static class MaxReversedComparator implements Comparator<JPlagComparison> {

    public int compare(JPlagComparison o1, JPlagComparison o2) {
      float p1 = o1.percentMaxAB();
      float p2 = o2.percentMaxAB();
      if (p1 == p2) {
        return 0;
      }
      if (p1 < p2) {
        return -1;
      } else {
        return 1;
      }
    }
  }

  public static class MinComparator implements Comparator<JPlagComparison> {

    public int compare(JPlagComparison o1, JPlagComparison o2) {
      float p1 = o1.percentMinAB();
      float p2 = o2.percentMinAB();
      if (p1 == p2) {
        return 0;
      }
      if (p1 > p2) {
        return -1;
      } else {
        return 1;
      }
    }
  }

  public static class MinReversedComparator implements Comparator<JPlagComparison> {

    public int compare(JPlagComparison o1, JPlagComparison o2) {
      float p1 = o1.percentMinAB();
      float p2 = o2.percentMinAB();
      if (p1 == p2) {
        return 0;
      }
      if (p1 < p2) {
        return -1;
      } else {
        return 1;
      }
    }
  }
}
