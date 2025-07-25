/*
 * Copyright Doma Tools Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.domaframework.doma.intellij.highlighter;

import com.intellij.icons.AllIcons.FileTypes;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.openapi.util.NlsContexts.ConfigurableName;
import java.util.Map;
import javax.swing.Icon;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SqlColorSettingsPage implements ColorSettingsPage {

  private static final AttributesDescriptor[] DESCRIPTORS =
      new AttributesDescriptor[] {

        // SQL Syntax
        new AttributesDescriptor("SQL//Literal//String", SqlSyntaxHighlighter.STRING),
        new AttributesDescriptor("SQL//Literal//Number", SqlSyntaxHighlighter.NUMBER),
        new AttributesDescriptor("SQL//Syntax//Keyword", SqlSyntaxHighlighter.KEYWORD),
        new AttributesDescriptor("SQL//Syntax//Function name", SqlSyntaxHighlighter.FUNCTION_NAME),
        new AttributesDescriptor("SQL//Syntax//DataType", SqlSyntaxHighlighter.DATATYPE),
        new AttributesDescriptor("SQL//Syntax//Other", SqlSyntaxHighlighter.OTHER),
        new AttributesDescriptor("SQL//Syntax//Word", SqlSyntaxHighlighter.WORD),
        new AttributesDescriptor("SQL//Comment//Line comment", SqlSyntaxHighlighter.LINE_COMMENT),
        new AttributesDescriptor(
            "SQL//Comment//Block content", SqlSyntaxHighlighter.BLOCK_COMMENT_CONTENT),
        // Doma Syntax
        new AttributesDescriptor("Expression//Identifier", SqlSyntaxHighlighter.EL_IDENTIFIER),
        new AttributesDescriptor("Expression//Literal//String", SqlSyntaxHighlighter.EL_STRING),
        new AttributesDescriptor("Expression//Literal//Character", SqlSyntaxHighlighter.EL_CHAR),
        new AttributesDescriptor("Expression//Literal//Number", SqlSyntaxHighlighter.EL_NUMBER),
        new AttributesDescriptor("Expression//Literal//Boolean", SqlSyntaxHighlighter.EL_BOOLEAN),
        new AttributesDescriptor("Expression//Literal//null", SqlSyntaxHighlighter.EL_NULL),
        // Directive
        new AttributesDescriptor("Expression//Directive//if", SqlSyntaxHighlighter.EL_IF),
        new AttributesDescriptor("Expression//Directive//for", SqlSyntaxHighlighter.EL_FOR),
        new AttributesDescriptor("Expression//Directive//end", SqlSyntaxHighlighter.EL_END),
        new AttributesDescriptor("Expression//Directive//elseIf", SqlSyntaxHighlighter.EL_ELSEIF),
        new AttributesDescriptor("Expression//Directive//expand", SqlSyntaxHighlighter.EL_EXPAND),
        new AttributesDescriptor(
            "Expression//Directive//populate", SqlSyntaxHighlighter.EL_POPULATE),
        // Symbol
        new AttributesDescriptor("Expression//Symbol//#", SqlSyntaxHighlighter.EL_HASH),
        new AttributesDescriptor("Expression//Symbol//@", SqlSyntaxHighlighter.EL_AT_SIGN),
        new AttributesDescriptor("Expression//Symbol//%", SqlSyntaxHighlighter.EL_PERCENT),
        new AttributesDescriptor("Expression//Symbol//^", SqlSyntaxHighlighter.EL_CARET),
        new AttributesDescriptor("Expression//Symbol//!", SqlSyntaxHighlighter.EL_NOT),
        new AttributesDescriptor("Expression//Symbol//:", SqlSyntaxHighlighter.EL_SEPARATOR),
        new AttributesDescriptor("Expression//Symbol//.", SqlSyntaxHighlighter.EL_DOT),
        new AttributesDescriptor("Expression//Symbol//,", SqlSyntaxHighlighter.EL_COMMA),
        new AttributesDescriptor("Expression//Symbol//(", SqlSyntaxHighlighter.EL_LEFT_PAREN),
        new AttributesDescriptor("Expression//Symbol//)", SqlSyntaxHighlighter.EL_RIGHT_PAREN),
        // Operator
        new AttributesDescriptor("Expression//Operator//new", SqlSyntaxHighlighter.EL_NEW),
        new AttributesDescriptor("Expression//Operator//+", SqlSyntaxHighlighter.EL_PLUS),
        new AttributesDescriptor("Expression//Operator//-", SqlSyntaxHighlighter.EL_MINUS),
        new AttributesDescriptor("Expression//Operator//*", SqlSyntaxHighlighter.EL_ASTERISK),
        new AttributesDescriptor("Expression//Operator///", SqlSyntaxHighlighter.EL_SLASH),
        new AttributesDescriptor("Expression//Operator//==", SqlSyntaxHighlighter.EL_EQ),
        new AttributesDescriptor("Expression//Operator//!=", SqlSyntaxHighlighter.EL_NE),
        new AttributesDescriptor("Expression//Operator//>=", SqlSyntaxHighlighter.GE),
        new AttributesDescriptor("Expression//Operator//<=", SqlSyntaxHighlighter.LE),
        new AttributesDescriptor("Expression//Operator//>", SqlSyntaxHighlighter.GT),
        new AttributesDescriptor("Expression//Operator//&&", SqlSyntaxHighlighter.EL_AND),
        new AttributesDescriptor("Expression//Operator//||", SqlSyntaxHighlighter.EL_OR),
        // Comment
        new AttributesDescriptor(
            "Expression//Comment//Block comment start", SqlSyntaxHighlighter.BLOCK_COMMENT_START),
        new AttributesDescriptor(
            "Expression//Comment//Block comment end", SqlSyntaxHighlighter.BLOCK_COMMENT_END),
      };

  @Override
  public @Nullable Icon getIcon() {
    return FileTypes.Dtd;
  }

  @Override
  public @NotNull SyntaxHighlighter getHighlighter() {
    return new SqlSyntaxHighlighter();
  }

  @Override
  public @NonNls @NotNull String getDemoText() {
    return """
                -- Sql Highlighter Demo
                /**
                  * Set highlights as you like
                  */
                UPDATE product
                   SET /*%populate*/
                       category = /* category */'category'
                       , expected_sales = /* price * pieces */0
                       , unit_price = /* purchase / pieces */0
                       /*%if mark != "XXX" */
                       , mark = /* mark */'mark'
                       /*%end */
                       /*%! This comment delete */
                       , employee_type = ( SELECT /*%expand "e" */*
                                             FROM employee e
                                            WHERE
                                                  /*%for name : names */
                                                  orderer = /* name */'orderer'
                                                    /*%if name.startWith("AA") && name.contains("_A") */
                                                      AND div = 'A'
                                                      AND rank >= 5
                                                    /*%elseif name.startWith("BB") || name.contains("_B") */
                                                      AND div = 'B'
                                                      AND rank > 2
                                                      AND rank < 5
                                                    /*%else */
                                                        AND div = 'C'
                                                    /*%end */
                                                    /*%if name_has_next */
                                                      /*# "OR" */
                                                    /*%end */
                                                  /*%end*/ )
                 WHERE type = /* @example.Type@Type.getValue() */'type'
                   AND cost_limit <= /* cost + 100 */0
                   AND cost_limit >= /* cost - 100 */99999;

                -- Demo Text2
                SELECT p.project_id
                       , p.project_name
                       , p.project_type
                       , p.project_category
                       , p.pre_project
                  FROM project p
                 WHERE p.project_type = /* new Project().type */'type'
                   AND (
                        /*%for project : preProjects */
                          /*%if project.category != null */
                            project_category = /* project.category.plus('t') */'category'
                          /*%elseif project.pre == true */
                            pre_project = /* project.preProjectId */0
                            /*%if project_has_next */
                              /*# "OR" */
                            /*%end */
                         /*%end */
                        /*%end */)

                -- DemoText3
                SELECT common
                       , amount
                       , date
                       , SUM(amount) OVER(PARTITION BY common ORDER BY date) AS common_amount
                  FROM ammount_table
                """;
  }

  @Override
  public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return Map.of();
  }

  @Override
  public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
    return DESCRIPTORS;
  }

  @Override
  public ColorDescriptor @NotNull [] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @Override
  public @NotNull @ConfigurableName String getDisplayName() {
    return "SQL（Doma Tools）";
  }
}
