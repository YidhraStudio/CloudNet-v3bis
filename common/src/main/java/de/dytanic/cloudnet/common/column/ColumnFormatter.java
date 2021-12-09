/*
 * Copyright 2019-2021 CloudNetService team & contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.dytanic.cloudnet.common.column;

import com.google.common.base.Verify;
import de.dytanic.cloudnet.common.StringUtil;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ColumnFormatter {

  private static final String ENTRY_FORMAT = "%s%s%s%s%s";

  private final String leftSpacer;
  private final String rightSpacer;

  private final char columnLeftBracket;
  private final char columnRightBracket;
  private final char headerValuesSpacerChar;

  private final String[] columnTitles;

  private volatile Collection<String> formattedColumnTitles;

  protected ColumnFormatter(
    @NotNull String leftSpacer,
    @NotNull String rightSpacer,
    char columnLeftBracket,
    char columnRightBracket,
    char headerValuesSpacerChar,
    @NotNull String[] columnTitles
  ) {
    this.leftSpacer = leftSpacer;
    this.rightSpacer = rightSpacer;
    this.columnLeftBracket = columnLeftBracket;
    this.columnRightBracket = columnRightBracket;
    this.headerValuesSpacerChar = headerValuesSpacerChar;
    this.columnTitles = columnTitles;
  }

  public static @NotNull Builder builder() {
    return new Builder();
  }

  public @NotNull Collection<String> formatLines(ColumnEntry @NotNull ... entries) {
    // no entries - no pain, just format the header
    if (entries.length == 0) {
      if (this.formattedColumnTitles == null) {
        // initial formatting of the titles
        Collection<String> result = new LinkedList<>();
        for (String columnTitle : this.columnTitles) {
          result.add(String.format(
            ENTRY_FORMAT,
            this.columnLeftBracket,
            this.leftSpacer,
            columnTitle,
            this.rightSpacer,
            this.columnRightBracket));
        }
        // cache the result
        this.formattedColumnTitles = result;
      }
      // use the cached formatted title
      return this.formattedColumnTitles;
    } else {
      // for each header find the longest entry (or use the header length if the header is longer)
      String[] spaceCache = new String[entries.length];
      // format the header
      StringBuilder builder = new StringBuilder();
      for (int i = 0; i < this.columnTitles.length; i++) {
        String title = this.columnTitles[i];
        int titleLength = title.length();
        // get the spaces we need to append
        String ourSpaces;
        // compute the cache - if the title is too short append to the title, otherwise append to the column value
        if (titleLength > entries[i].getColumnMinLength()) {
          ourSpaces = "";
          spaceCache[i] = StringUtil.repeat(' ', titleLength - entries[i].getColumnMinLength()).intern();
        } else {
          ourSpaces = StringUtil.repeat(' ', entries[i].getColumnMinLength() - titleLength).intern();
          spaceCache[i] = "";
        }
        // print the header entry
        builder
          .append(this.columnLeftBracket)
          .append(this.leftSpacer)
          .append(this.columnTitles[i])
          .append(ourSpaces)
          .append(this.rightSpacer)
          .append(this.columnRightBracket);
      }
      // the result cache - it contains each row
      List<String> result = new LinkedList<>();
      result.add(builder.toString());
      // the second line is the spacer between the header and the entries
      result.add(StringUtil.repeat(this.headerValuesSpacerChar, builder.length()));
      // reset the string builder
      builder.setLength(0);
      // get the amount of times we need to loop to fill everything
      int repeatCount = this.columnTitles.length * entries[0].getFormattedEntries().length;
      // format each row
      int currentDepth = 0;
      for (int i = 0; i <= repeatCount; i++) {
        // step the depth if required
        if (i != 0 && i % this.columnTitles.length == 0) {
          // append the build line to the result
          result.add(builder.toString());
          // reset the string builder
          builder.setLength(0);
          // step the depth
          currentDepth++;
          // stop here if it was the last run - we just need to flush the buffer
          if (i == repeatCount) {
            break;
          }
        }
        // get the index of the entry we want to print
        int index = i - (this.columnTitles.length * currentDepth);
        if (index < 0) {
          // we are still in the first row
          index = i;
        }
        // get the amount of spaces needed to print so that the column looks nice
        String spaces = spaceCache[index];
        // append the current entry
        builder
          .append(this.columnLeftBracket)
          .append(this.leftSpacer)
          .append(entries[index].getFormattedEntries()[currentDepth])
          .append(spaces)
          .append(this.rightSpacer)
          .append(this.columnRightBracket);
      }
      // formatting completed
      return result;
    }
  }

  public static final class Builder {

    private String leftSpacer = " ";
    private String rightSpacer = "";

    private char columnLeftBracket = '|';
    private char columnRightBracket = ' ';
    private char headerValuesSpacerChar = '_';

    private List<String> columnTitles = new LinkedList<>();

    public @NotNull Builder leftSpacer(@NotNull String leftSpacer) {
      this.leftSpacer = leftSpacer;
      return this;
    }

    public @NotNull Builder rightSpacer(@NotNull String rightSpacer) {
      this.rightSpacer = rightSpacer;
      return this;
    }

    public @NotNull Builder columnLeftBracket(char columnLeftBracket) {
      this.columnLeftBracket = columnLeftBracket;
      return this;
    }

    public @NotNull Builder columnRightBracket(char columnRightBracket) {
      this.columnRightBracket = columnRightBracket;
      return this;
    }

    public @NotNull Builder headerValuesSpacerChar(char headerValuesSpacerChar) {
      this.headerValuesSpacerChar = headerValuesSpacerChar;
      return this;
    }

    public @NotNull Builder columnTitles(@NotNull List<String> columnTitles) {
      this.columnTitles = new LinkedList<>(columnTitles);
      return this;
    }

    public @NotNull Builder columnTitles(@NotNull String... columnTitles) {
      this.columnTitles = Arrays.asList(columnTitles);
      return this;
    }

    public @NotNull ColumnFormatter build() {
      Verify.verify(!this.columnTitles.isEmpty(), "At least one title must be given");
      return new ColumnFormatter(
        this.leftSpacer,
        this.rightSpacer,
        this.columnLeftBracket,
        this.columnRightBracket,
        this.headerValuesSpacerChar,
        this.columnTitles.toArray(new String[0]));
    }
  }
}
