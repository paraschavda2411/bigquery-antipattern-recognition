package com.google.zetasql.toolkit.antipattern.parser.visitors.whereorder;

import com.google.zetasql.parser.ASTNodes;
import com.google.zetasql.parser.ASTNodes.ASTExpression;
import com.google.zetasql.parser.ParseTreeVisitor;
import com.google.zetasql.toolkit.antipattern.util.ZetaSQLStringParsingHelper;
import java.util.ArrayList;
import java.util.Stack;

public class IdentifyWhereOrderVisitor extends ParseTreeVisitor {

  private String query;
  private Boolean insideWhere = false;
  private final String WHERE_ORDER_SUGGESTION_MESSAGE = "LIKE filter in line %d precedes a more selective filter.";
  private ArrayList<String> result = new ArrayList<String>();
  private Stack<ASTNodes.ASTWhereClause> whereNodeStack = new Stack<>();
  public IdentifyWhereOrderVisitor(String query) {
    this.query = query;
  }

  @Override
  public void visit(ASTNodes.ASTWhereClause whereNode) {
    if(whereNode.getExpression() instanceof ASTNodes.ASTAndExpr) {
      CheckAndInWhereVisitor checkAndInWhereVisitor = new CheckAndInWhereVisitor(query);
      whereNode.accept(checkAndInWhereVisitor);
      ASTExpression likeFilterBeforeEqualFilter = checkAndInWhereVisitor.getLikeFilterBeforeEqualFilter();
      if(likeFilterBeforeEqualFilter!=null){
        int lineNum = ZetaSQLStringParsingHelper.countLine(query, likeFilterBeforeEqualFilter.getParseLocationRange().start());
        result.add(String.format(WHERE_ORDER_SUGGESTION_MESSAGE, lineNum));
      }
    }
    super.visit(whereNode);
  }

  public ArrayList<String> getResult() {
    return result;
  }
}
