// This is a generated file. Not intended for manual editing.
package generated.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static generated.KotlinTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import generated.psi.*;

public class KtPrefixUnaryOperationImpl extends ASTWrapperPsiElement implements KtPrefixUnaryOperation {

  public KtPrefixUnaryOperationImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof KtVisitor) ((KtVisitor)visitor).visitPrefixUnaryOperation(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public KtFloatConstant getFloatConstant() {
    return findChildByClass(KtFloatConstant.class);
  }

  @Override
  @Nullable
  public KtIntegerConstant getIntegerConstant() {
    return findChildByClass(KtIntegerConstant.class);
  }

  @Override
  @Nullable
  public KtAnnotatedExpression getAnnotatedExpression() {
    return findChildByClass(KtAnnotatedExpression.class);
  }

  @Override
  @Nullable
  public KtArrayAccess getArrayAccess() {
    return findChildByClass(KtArrayAccess.class);
  }

  @Override
  @Nullable
  public KtBinaryConstant getBinaryConstant() {
    return findChildByClass(KtBinaryConstant.class);
  }

  @Override
  @Nullable
  public KtCallSuffix getCallSuffix() {
    return findChildByClass(KtCallSuffix.class);
  }

  @Override
  @Nullable
  public KtCallableReference getCallableReference() {
    return findChildByClass(KtCallableReference.class);
  }

  @Override
  @Nullable
  public KtDotQualifiedExpression getDotQualifiedExpression() {
    return findChildByClass(KtDotQualifiedExpression.class);
  }

  @Override
  @Nullable
  public KtElvisAccessExpression getElvisAccessExpression() {
    return findChildByClass(KtElvisAccessExpression.class);
  }

  @Override
  @Nullable
  public KtFunctionLiteralExpression getFunctionLiteralExpression() {
    return findChildByClass(KtFunctionLiteralExpression.class);
  }

  @Override
  @Nullable
  public KtIfExpression getIfExpression() {
    return findChildByClass(KtIfExpression.class);
  }

  @Override
  @Nullable
  public KtJumpBreak getJumpBreak() {
    return findChildByClass(KtJumpBreak.class);
  }

  @Override
  @Nullable
  public KtJumpContinue getJumpContinue() {
    return findChildByClass(KtJumpContinue.class);
  }

  @Override
  @Nullable
  public KtJumpReturn getJumpReturn() {
    return findChildByClass(KtJumpReturn.class);
  }

  @Override
  @Nullable
  public KtJumpThrow getJumpThrow() {
    return findChildByClass(KtJumpThrow.class);
  }

  @Override
  @NotNull
  public List<KtLabel> getLabelList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, KtLabel.class);
  }

  @Override
  @Nullable
  public KtLoop getLoop() {
    return findChildByClass(KtLoop.class);
  }

  @Override
  @Nullable
  public KtObjectLiteral getObjectLiteral() {
    return findChildByClass(KtObjectLiteral.class);
  }

  @Override
  @Nullable
  public KtParenthesizedExpression getParenthesizedExpression() {
    return findChildByClass(KtParenthesizedExpression.class);
  }

  @Override
  @Nullable
  public KtPrefixOperation getPrefixOperation() {
    return findChildByClass(KtPrefixOperation.class);
  }

  @Override
  @Nullable
  public KtPrefixUnaryOperation getPrefixUnaryOperation() {
    return findChildByClass(KtPrefixUnaryOperation.class);
  }

  @Override
  @Nullable
  public KtReferenceExpression getReferenceExpression() {
    return findChildByClass(KtReferenceExpression.class);
  }

  @Override
  @Nullable
  public KtSafeAccessExpression getSafeAccessExpression() {
    return findChildByClass(KtSafeAccessExpression.class);
  }

  @Override
  @Nullable
  public KtStringTemplate getStringTemplate() {
    return findChildByClass(KtStringTemplate.class);
  }

  @Override
  @Nullable
  public KtThisExpression getThisExpression() {
    return findChildByClass(KtThisExpression.class);
  }

  @Override
  @Nullable
  public KtTryBlock getTryBlock() {
    return findChildByClass(KtTryBlock.class);
  }

  @Override
  @Nullable
  public KtType getType() {
    return findChildByClass(KtType.class);
  }

  @Override
  @Nullable
  public KtWhen getWhen() {
    return findChildByClass(KtWhen.class);
  }

}
