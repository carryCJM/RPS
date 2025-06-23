package oo.com.iseu.CASTHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import oo.com.iseu.Information.DeclarePosition;
import oo.com.iseu.Information.ThreadType;

public class CASTHelper {
	private HashSet<String> varType = new HashSet<>();
	private final static CASTHelper CAST_HELPER = new CASTHelper();
	private CASTHelper() {	
		varType.add("String");
		varType.add("Short");
		varType.add("Long");
		varType.add("Integer");
		varType.add("Float");
		varType.add("Double");
		varType.add("Character");
		varType.add("Byte");
		varType.add("Boolean");
	}
	public static CASTHelper getInstance() {
		return CAST_HELPER;
	}
	/**��������λ�õĴ���,�����õ�������������������λ��
	 * 
	 * @param decNode�����������ڵ�
	 * @return ���������λ��
	 */
	public DeclarePosition varDeclaredPositionHandle(ASTNode decNode) {
		//1.��Ա����
		if (decNode instanceof VariableDeclarationFragment) {   
			//(1).��ͨ��Ա����
			if (decNode.getParent() instanceof FieldDeclaration) { 
				boolean isFinal = false;
				FieldDeclaration fieldDeclaration = (FieldDeclaration)decNode.getParent();
				List<?> list = fieldDeclaration.modifiers();
				for (Object object : list) {
					if (object instanceof Modifier&&((Modifier)object).isFinal()) {
						isFinal = true;
					}
				}
				//���ɸ����ͻ�ԭʼ����Ϊfinal
				if (varType.contains(fieldDeclaration.getType().resolveBinding().getName())||
					(fieldDeclaration.getType().isPrimitiveType()&&isFinal)	) {
					return DeclarePosition.INMETHOD;
				}
				if (fieldDeclaration.getType().isPrimitiveType()) {
					return DeclarePosition.INMEMBERPRIMITIVE;
				}
				return DeclarePosition.INMEMBER;
			}
			//(2)�ں����ڲ�
			else if (decNode.getParent() instanceof VariableDeclarationStatement&&methodKey(decNode)!=null) {
				if (decNode.getParent() instanceof VariableDeclarationStatement) {  // int a = 3;ԭʼ�����򲻿ɱ����
					VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement)decNode.getParent();
					if (variableDeclarationStatement.getType().isPrimitiveType()||
						varType.contains(variableDeclarationStatement.getType().resolveBinding().getName())) {
						return DeclarePosition.INMETHOD;
					}
				}
				else if(decNode.getParent() instanceof VariableDeclarationExpression){//for(int a=3;...)
					VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression)decNode.getParent();
					if (variableDeclarationExpression.getType().isPrimitiveType()||
						varType.contains(variableDeclarationExpression.getType().resolveBinding().getName())) {
						return DeclarePosition.INMETHOD;
					}
				}
				return DeclarePosition.INMETHODMEM;
			}
		}
		//2.��������
		else if (decNode instanceof SingleVariableDeclaration&&decNode.getParent() instanceof MethodDeclaration) {  
			SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration)decNode;
			if (singleVariableDeclaration.getType().isPrimitiveType()||
				varType.contains(singleVariableDeclaration.getType().resolveBinding().getName())) {
				return DeclarePosition.INMETHOD;
			}
			return DeclarePosition.INPARAMETER;
		}
		//���ǳ�Ա�����Ͳ�������������javaԴ���еı�����null�Ҳ��������ΪINMETHOD����������
		return DeclarePosition.INMETHOD;
	}
	/**�鿴ASTNode�б���������λ��
	 * 
	 * @param astNode ������
	 * @return ����λ��
	 */
	public DeclarePosition varDeclaredPosition(ASTNode astNode) {
		if (astNode==null) {
			return DeclarePosition.INMETHOD;
		}
		CompilationUnit compilationUnit = (CompilationUnit) astNode.getRoot();
		//1.SimpleName__var
		if (astNode instanceof SimpleName) {                            
			SimpleName simpleName = (SimpleName) astNode;
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			//��������еı�������������
			if (astNode.getParent() == decNode) {
				return DeclarePosition.INMETHOD;
			}
			return varDeclaredPositionHandle(decNode);
		}
		//2.QualifiedName__obj.var
		else if(astNode instanceof QualifiedName) {                      
			QualifiedName qualifiedName = (QualifiedName) astNode;
			//�ҵ���������Ǹ�����xx.x.i�е�xx
			if(!(qualifiedName.getQualifier() instanceof SimpleName)){
				return varDeclaredPosition(qualifiedName.getQualifier());
			}
			ASTNode decNode = compilationUnit.findDeclaringNode(qualifiedName.getQualifier().resolveBinding());
			return varDeclaredPositionHandle(decNode);
		}
		//3.FieldAccess__this.var
		else if (astNode instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess) astNode;
			//this.a.b�ҵ���������Ǹ�����
			if (!(fieldAccess.getExpression() instanceof SimpleName)) {
				return varDeclaredPosition(fieldAccess.getExpression());
			}
			SimpleName simpleName = (SimpleName) fieldAccess.getName();
			ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
			return varDeclaredPositionHandle(decNode);
		}
		//4.ArrayAccess__a[20]
		else if (astNode instanceof ArrayAccess	) {
			ArrayAccess access = (ArrayAccess) astNode;
			return varDeclaredPosition(access.getArray());
		}
		//5.SuperFieldAccess__super.var
		else if (astNode instanceof SuperFieldAccess) {
			SuperFieldAccess superFieldAccess = (SuperFieldAccess) astNode;
			return varDeclaredPosition(superFieldAccess.getName());
		}
		return DeclarePosition.INMETHOD;
	}
	
	/**
	 * �õ�simpleName���������ձ���ȫ��
	 * @param astNode �����ڵ�
	 * @return  ����ȫ��
	 */
	public ASTNode getVarFullName(ASTNode astNode) {
		ASTNode pNode = astNode;
		//1.obj.name
		if(pNode.getParent() instanceof QualifiedName){
			do {
				pNode = pNode.getParent();
			} while (pNode.getParent() instanceof QualifiedName);	
			return pNode;
		}
		//2.this.name
		else if (pNode.getParent() instanceof FieldAccess) {
			do {
				pNode = pNode.getParent();
			} while (pNode.getParent() instanceof FieldAccess);	
			return pNode;
		}
		//3.super.name
		else if (pNode.getParent() instanceof SuperFieldAccess) {
			do {
				pNode = pNode.getParent();
			} while (pNode.getParent() instanceof SuperFieldAccess);	
			return pNode;
		}
		
		//4.simepleName
		return astNode;
	}
	
	/**
	 * �õ�ȫ����������
	 * @param astNode ȫ���ڵ�
	 * @return  ȫ�������������
	 */
	public ASTNode getLeftVarName(ASTNode astNode) {
		//1.SimpleName__var
		if (astNode instanceof SimpleName) {                            
			return astNode;
		}
		//2.QualifiedName__obj.var
		else if(astNode instanceof QualifiedName) {                      
			QualifiedName qualifiedName = (QualifiedName) astNode;
			//�ҵ���������Ǹ�����xx.x.i�е�xx
			if(!(qualifiedName.getQualifier() instanceof SimpleName)){
				return getLeftVarName(qualifiedName.getQualifier());
			}
			return qualifiedName.getQualifier();
		}
		//3.FieldAccess__this.var
		else if (astNode instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess) astNode;
			//this.a.b�ҵ���������Ǹ�����
			if (!(fieldAccess.getExpression() instanceof ThisExpression)) {
				return getLeftVarName(fieldAccess.getExpression());
			}
			return fieldAccess.getName();
		}
		//4.SuperFieldAccess__super.var
		else if (astNode instanceof SuperFieldAccess) {
			SuperFieldAccess superFieldAccess = (SuperFieldAccess) astNode;
			return superFieldAccess.getName();
		}
		//5.ArrayAccess buffer[tail++]  ȡbuffer
		else if (astNode instanceof ArrayAccess) {
			ArrayAccess access = (ArrayAccess)astNode;
			return getLeftVarName(access.getArray());
		}
		else if (astNode instanceof ExpressionStatement) {
			ExpressionStatement expressionStatement = (ExpressionStatement)astNode;
			return getLeftVarName(expressionStatement.getExpression());
		}
		else if (astNode instanceof MethodInvocation){
			 MethodInvocation methodInvocation = (MethodInvocation)astNode;
			 return getLeftVarName(methodInvocation.getExpression());
		}else if (astNode instanceof ParenthesizedExpression) {
			ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) astNode;
			if (parenthesizedExpression != null) {
				ASTNode node = parenthesizedExpression.getExpression();
				if (node instanceof CastExpression) {
					CastExpression castExpression = (CastExpression) node;
					return getLeftVarName(castExpression.getExpression());
				}
			}
		}
		return null;
	}

	public ASTNode getRightVarName(ASTNode astNode) {
		//1.SimpleName__var
		if (astNode instanceof SimpleName) {                            
			return astNode;
		}
		//2.QualifiedName__obj.var
		else if(astNode instanceof QualifiedName) {                      
			QualifiedName qualifiedName = (QualifiedName) astNode;
			return qualifiedName.getName();
		}
		//3.FieldAccess__this.var
		else if (astNode instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess) astNode;
			return getRightVarName(fieldAccess.getName());
		}
		//4.SuperFieldAccess__super.var
		else if (astNode instanceof SuperFieldAccess) {
			SuperFieldAccess superFieldAccess = (SuperFieldAccess) astNode;
			return getRightVarName(superFieldAccess.getName());
		}
		//5.ArrayAccess buffer[tail++]  ȡbuffer
		else if (astNode instanceof ArrayAccess) {
			ArrayAccess access = (ArrayAccess)astNode;
			return getRightVarName(access.getArray());
		}
		else if (astNode instanceof ExpressionStatement) {
			ExpressionStatement expressionStatement = (ExpressionStatement)astNode;
			return getRightVarName(expressionStatement.getExpression());
		}
		else if (astNode instanceof MethodInvocation){
			 MethodInvocation methodInvocation = (MethodInvocation)astNode;
			 return getRightVarName(methodInvocation.getExpression());
		}
		return null;
	}
	/**
	 * �õ�ȫ����������
	 * @param astNode ȫ���ڵ�
	 * @return ȫ�������ұ�����
	 */
	public String getObjectName(ASTNode astNode) {
		//1.SimpleName__var
		if (astNode instanceof SimpleName&&((SimpleName)astNode).resolveTypeBinding()!=null) {                            
			return ((SimpleName)astNode).resolveTypeBinding().getBinaryName();
		}
		//2.QualifiedName__obj.var
		else if(astNode instanceof QualifiedName) {                      
			QualifiedName qualifiedName = (QualifiedName) astNode;
			return getObjectName(qualifiedName.getName());
		}
		//3.FieldAccess__this.var
		else if (astNode instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess) astNode;
			return getObjectName(fieldAccess.getName());
		}
		//4.SuperFieldAccess__super.var
		else if (astNode instanceof SuperFieldAccess) {
			SuperFieldAccess superFieldAccess = (SuperFieldAccess) astNode;
			return getObjectName(superFieldAccess.getName());
		}
		//5.ArrayAccess buffer[tail++]  ȡbuffer
		else if (astNode instanceof ArrayAccess) {
			ArrayAccess access = (ArrayAccess)astNode;
			return getObjectName(access.getArray());
		}
		else if (astNode instanceof ExpressionStatement) {
			ExpressionStatement expressionStatement = (ExpressionStatement)astNode;
			return getObjectName(expressionStatement.getExpression());
		}
		else if (astNode instanceof MethodInvocation){
			 MethodInvocation methodInvocation = (MethodInvocation)astNode;
			 return getObjectName(methodInvocation.getExpression());
		}
		return null;
	}
	
	/**
	 * ��ȡ������յõ��Ķ����ITypeBinding
	 * @param astNode ASTNode
	 * @return  �����ITypeBinding
	 */
	public ITypeBinding getResolveTypeBinding(ASTNode astNode) {
		//1.SimpleName__var
		if (astNode instanceof SimpleName&&((SimpleName)astNode).resolveTypeBinding()!=null) {                            
			return ((SimpleName)astNode).resolveTypeBinding();
		}
		//2.QualifiedName__obj.var
		else if(astNode instanceof QualifiedName) {                      
			QualifiedName qualifiedName = (QualifiedName) astNode;
			return getResolveTypeBinding(qualifiedName.getName());
		}
		//3.FieldAccess__this.var
		else if (astNode instanceof FieldAccess) {
			FieldAccess fieldAccess = (FieldAccess) astNode;
			return getResolveTypeBinding(fieldAccess.getName());
		}
		//4.SuperFieldAccess__super.var
		else if (astNode instanceof SuperFieldAccess) {
			SuperFieldAccess superFieldAccess = (SuperFieldAccess) astNode;
			return getResolveTypeBinding(superFieldAccess.getName());
		}
		//5.ArrayAccess buffer[tail++]  ȡbuffer
		else if (astNode instanceof ArrayAccess) {
			ArrayAccess access = (ArrayAccess)astNode;
			return getResolveTypeBinding(access.getArray());
		}
		else if (astNode instanceof ExpressionStatement) {
			ExpressionStatement expressionStatement = (ExpressionStatement)astNode;
			return getResolveTypeBinding(expressionStatement.getExpression());
		}
		else if (astNode instanceof MethodInvocation&&((MethodInvocation)astNode).resolveMethodBinding()!=null){
			 MethodInvocation methodInvocation = (MethodInvocation)astNode;
			 return methodInvocation.resolveMethodBinding().getReturnType();
		}
		return null;
	}
	/**
	 * �ж����Ͱ��Ƿ�Ϊ�߳���ص�����
	 * @param node �����Ͱ󶨽ڵ�
	 * @return �жϽ��
	 */
	public boolean isThreadRelate(ITypeBinding node) {
		if (node==null) {
			return false;
		}
		if (node.getKey().equals("Ljava/lang/Runnable;")) {
			return true;
		}
		//�鿴����
		if (node.getSuperclass()!=null) {
			String supClass = node.getSuperclass().getName();
			/*������ʽƥ��,����ƥ��RecursiveTask<T>
			 * 1.��д������ʽ
			 * 2.����������ʽ
			 * 3.�ñ����ģʽƥ���ַ����������������ƥ����
			 */
			String rTask ="RecursiveTask<.*>";
		    Pattern recursiveTaskPattern = Pattern.compile(rTask);
		    Matcher rMatcher = recursiveTaskPattern.matcher(supClass);
		    //������ʽƥ��,����ƥ��FutureTask<T>
		    String futureTask = "FutureTask<.*>";
		    Pattern futureTaskPattern = Pattern.compile(futureTask);
		    Matcher fmMatcher = futureTaskPattern.matcher(supClass);
			if (supClass.equals("Thread")||supClass.equals("RecursiveAction")||rMatcher.find()||fmMatcher.find()) { 
				return true;
			}
			else if (isThreadRelate(node.getSuperclass())) {
				return true;
			}
		}
		//�鿴�ӿ�
		ITypeBinding[] interfaces = node.getInterfaces();
		if (interfaces.length>0) {
			ArrayList<String> interfaceNames = new ArrayList<>();
			//������ʽƥ�䣬Callable<T>
			String callableMatch = "Callable<.*>";
			Pattern callablePattern = Pattern.compile(callableMatch);
			//������ʽƥ�䣬Future<T>
			String futureMatch = "Future<.*>";
			Pattern futurePattern = Pattern.compile(futureMatch);
			//������ʽƥ�䣬RunnableFuture<T>
			String rFutureMatch = "RunnableFuture<.*>";
			Pattern rFuturePattern = Pattern.compile(rFutureMatch);
			for(int i=0;i<interfaces.length;i++){
				interfaceNames.add(interfaces[i].getName());			
			}
			for (String interfaceName : interfaceNames) {
				Matcher cMatcher = callablePattern.matcher(interfaceName);
				Matcher fMatcher = futurePattern.matcher(interfaceName);
				Matcher rMatcher = rFuturePattern.matcher(interfaceName);
				if (interfaceName.equals("Future")||interfaceName.equals("Runnable")||cMatcher.find()||fMatcher.find()||rMatcher.find()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * ��ȡ�߳�����
	 * @param node �����Ͱ󶨽ڵ�
	 * @return �߳�����
	 */
	public ThreadType acquireThreadType(ITypeBinding node) {
		if (node==null) {
			return null;
		}
		if (node.getKey().equals("Ljava/lang/Runnable;")) {
			return ThreadType.RUNNABLE;
		}
		//�鿴����
		if (node.getSuperclass()!=null) {
			String supClass = node.getSuperclass().getName();
			/*������ʽƥ��,����ƥ��RecursiveTask<T>
			 * 1.��д������ʽ
			 * 2.����������ʽ
			 * 3.�ñ����ģʽƥ���ַ����������������ƥ����
			 */
			String rTask ="RecursiveTask<.*>";
		    Pattern recursiveTaskPattern = Pattern.compile(rTask);
		    Matcher rMatcher = recursiveTaskPattern.matcher(supClass);
		    if (supClass.equals("Thread")) {
				return ThreadType.THREAD;
			}
		    else if (supClass.equals("RecursiveAction")) {
				return ThreadType.RECURSIVEACTION;
			}
		    else if (rMatcher.find()) {
				return ThreadType.RECURSIVETASK;
			}
			else if (isThreadRelate(node.getSuperclass())) {
				return acquireThreadType(node.getSuperclass());
			}
		}
		//�鿴�ӿ�
		ITypeBinding[] interfaces = node.getInterfaces();
		if (interfaces.length>0) {
			ArrayList<String> interfaceNames = new ArrayList<>();
			//������ʽƥ�䣬Callable<T>
			String callableMatch = "Callable<.*>";
			Pattern callablePattern = Pattern.compile(callableMatch);
			for(int i=0;i<interfaces.length;i++){
				interfaceNames.add(interfaces[i].getName());			
			}
			for (String interfaceName : interfaceNames) {
				Matcher cMatcher = callablePattern.matcher(interfaceName);
				if (interfaceName.equals("Runnable")) {
					return ThreadType.RUNNABLE;
				}
				else if (cMatcher.find()) {
					return ThreadType.CALLABLE;
				}
			}
		}
		return null;
	}
	/**
	 * ��ȡ������ں����ĺ�����
	 * @param node
	 * @return
	 */
	public String getMethodName(ASTNode node) {
		ASTNode root = node.getRoot();
		for(ASTNode astNode = node;astNode!=root;astNode = astNode.getParent()){
			if (astNode instanceof MethodDeclaration) {
				MethodDeclaration methodDeclaration = (MethodDeclaration) astNode;
				return methodDeclaration.getName().getIdentifier();
			}
		}
		return null;
	}
	/**
	 *  ��ȡNode���ں�����KEY
	 * @param node
	 * @return
	 */
	public String methodKey(ASTNode node) {	
		if (node==null) {
			return null;
		}
		//��ȡ����         < ������+�����б�>   ��Ϊ���������������
		ASTNode pNode = node;
		while(!(pNode instanceof MethodDeclaration)){
			if (pNode instanceof Initializer||pNode==node.getRoot()) {   //���ڳ�ʼ���飬���ں�������ֱ�ӷ���
				return null;
			}
			pNode = pNode.getParent();
		} 
		MethodDeclaration methodDeclaration = (MethodDeclaration)pNode;
		//����ǹ��캯����������
//		if (methodDeclaration.isConstructor()) {
//			return null;
//		}
		StringBuilder methodName = new StringBuilder(methodDeclaration.getName().toString());   //������                             
		List<?> parameters  = methodDeclaration.parameters();  //�����к�
		for (Object object : parameters) {
			if (object instanceof SingleVariableDeclaration	) {
				SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration)object;
				methodName.append("_"+singleVariableDeclaration.getType().toString());
			}
		}
		//��ȡ���ڵ�   <��+��>  �����������������
		String className ="";
		ASTNode classNode = methodDeclaration.getParent();
		if (classNode instanceof TypeDeclaration) {
			TypeDeclaration typeDeclaration = (TypeDeclaration) classNode;
			if (typeDeclaration.resolveBinding()!=null) {
				className = typeDeclaration.resolveBinding().getBinaryName();
			}
			else {
				return null;
			}		
		}
		else if (classNode instanceof AnonymousClassDeclaration) {
			AnonymousClassDeclaration anonymousClassDeclaration = (AnonymousClassDeclaration) classNode;
			if (anonymousClassDeclaration.resolveBinding()!=null) {
				className = anonymousClassDeclaration.resolveBinding().getBinaryName();
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
		if (className==null||className.equals("")) {
			return null;
		}
		int dotPosition = className.lastIndexOf('.');
		if (dotPosition==-1) {
			return null;
		}
		return className.substring(0, dotPosition)+"_"+className.substring(dotPosition+1)+"_"+methodName;
	}
	
	/**
	 * ��ȡastNode���ڵĺ��������ڵ�
	 * @param node  : ����ASTNode
	 * @return  ���ڵĺ��������ڵ�
	 */
	public MethodDeclaration getMethodDeclaration(ASTNode node) {
		if (node==null) {
			return null;
		}
		ASTNode pNode = node;
		while(!(pNode instanceof MethodDeclaration)){
			if (pNode==node.getRoot()) {   //���ں�������ֱ�ӷ���
				return null;
			}
			pNode = pNode.getParent();
		} 
		return (MethodDeclaration)pNode;
	}
	/**
	 * ��ȡ���ú�����key
	 * @param astNode  MethodInvocaton����SuperMethodInvocation
	 * @return key (��·��+����+������+���������б�)
	 */
	public String getInvokeMethodKey(ASTNode astNode) {
		if (astNode instanceof MethodInvocation) {
			MethodInvocation node = (MethodInvocation)astNode;
			if (node.resolveMethodBinding()!=null) {
				StringBuilder methodName = new StringBuilder(node.resolveMethodBinding().getMethodDeclaration().getName());
				//��·��+����
				String className = node.resolveMethodBinding().getDeclaringClass().getBinaryName();
				ITypeBinding[] typeBinding = node.resolveMethodBinding().getMethodDeclaration().getParameterTypes();
				for (ITypeBinding iTypeBinding : typeBinding) {
					String s = iTypeBinding.getName();
//					if (s.contains("[")) {
//						s = s.substring(0, s.indexOf('['));
//					}
					methodName.append("_" + s);
				}
				if (className==null||className.equals("")) {
					return null;
				}
				int dotPosition = className.lastIndexOf('.');
				if (dotPosition==-1) {
					return null;
				}
				return className.substring(0, dotPosition)+"_"+className.substring(dotPosition+1)+"_"+methodName;
			}
		}
		else if (astNode instanceof SuperMethodInvocation) {
			SuperMethodInvocation node = (SuperMethodInvocation)astNode;
			if (node.resolveMethodBinding()!=null) {
				StringBuilder methodName = new StringBuilder(node.resolveMethodBinding().getMethodDeclaration().getName());
				//��·��+����
				String className = node.resolveMethodBinding().getDeclaringClass().getBinaryName();
				//������+���������б�
				ITypeBinding[] typeBinding = node.resolveMethodBinding().getMethodDeclaration().getParameterTypes();
				for (ITypeBinding iTypeBinding : typeBinding) {
					methodName.append("_" + iTypeBinding.getName().toString());
				}
				if (className==null||className.equals("")) {
					return null;
				}
				int dotPosition = className.lastIndexOf('.');
				if (dotPosition==-1) {
					return null;
				}
				return className.substring(0, dotPosition)+"_"+className.substring(dotPosition+1)+"_"+methodName;
			}
		}
		else if (astNode instanceof ConstructorInvocation) {
			ConstructorInvocation node = (ConstructorInvocation) astNode;
			if (node.resolveConstructorBinding()!=null) {
				StringBuilder methodName = new StringBuilder(node.resolveConstructorBinding().getMethodDeclaration().getName());
				//��·��+����
				String className = node.resolveConstructorBinding().getDeclaringClass().getBinaryName();
				//������+���������б�
				ITypeBinding[] typeBinding = node.resolveConstructorBinding().getMethodDeclaration().getParameterTypes();
				for (ITypeBinding iTypeBinding : typeBinding) {
					methodName.append("_" + iTypeBinding.getName().toString());
				}
				if (className==null||className.equals("")) {
					return null;
				}
				int dotPosition = className.lastIndexOf('.');
				if (dotPosition==-1) {
					return null;
				}
				return className.substring(0, dotPosition)+"_"+className.substring(dotPosition+1)+"_"+methodName;
			}
		}
		else if (astNode instanceof SuperConstructorInvocation) {
			SuperConstructorInvocation node = (SuperConstructorInvocation) astNode;
			if (node.resolveConstructorBinding()!=null) {
				StringBuilder methodName = new StringBuilder(node.resolveConstructorBinding().getMethodDeclaration().getName());
				//��·��+����
				String className = node.resolveConstructorBinding().getDeclaringClass().getBinaryName();
				//������+���������б�
				ITypeBinding[] typeBinding = node.resolveConstructorBinding().getMethodDeclaration().getParameterTypes();
				for (ITypeBinding iTypeBinding : typeBinding) {
					methodName.append("_" + iTypeBinding.getName().toString());
				}
				if (className==null||className.equals("")) {
					return null;
				}
				int dotPosition = className.lastIndexOf('.');
				if (dotPosition==-1) {
					return null;
				}
				return className.substring(0, dotPosition)+"_"+className.substring(dotPosition+1)+"_"+methodName;
			}
		}
		return null;
	}
	//���SimpleName��declared�ڵ�
	public ASTNode getDecNode(SimpleName simpleName) {
		CompilationUnit compilationUnit = (CompilationUnit) simpleName.getRoot();
		ASTNode decNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
		return decNode;
	}
	//���ز������ں����Ĳ����б��е�index ��0��ʼ
	public int getParaIndex(SimpleName simpleName) {
		ASTNode astNode = getDecNode(simpleName);
		if (astNode instanceof SingleVariableDeclaration) {
			SingleVariableDeclaration node = (SingleVariableDeclaration)astNode;
			if (node.getParent() instanceof MethodDeclaration) {
				MethodDeclaration methodDeclaration = (MethodDeclaration) node.getParent();
				List<?> parameters = methodDeclaration.parameters();
				int position = 0;
				for(int i=0;i<parameters.size();i++){
					if (parameters.get(i) instanceof SingleVariableDeclaration) {
						SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration)parameters.get(i);
						if(singleVariableDeclaration.getName().toString().equals(node.getName().toString())){
							position = i;
							break;
						}
					}	
				}
				return position;
			}
		}
		return -1;
	}
	/**
	 * ����simpleName���ں������õ�λ��(-1����-2Ϊ����0~nΪindex)
	 * simpleName:Ҫ��ѯ�ı������ڵ�
	 * methodInvoke:�������ڵĺ������ýڵ�
	 */
	public int getIndexInMethodInvoke(SimpleName simpleName,ASTNode methodInvoke) {
		if (methodInvoke instanceof MethodInvocation) {
			MethodInvocation methodInvocation = (MethodInvocation)methodInvoke;
			ASTNode astNode = methodInvocation.getExpression();
			if (astNode!=null) {
				SimpleName varName = (SimpleName)getLeftVarName(astNode);
				//��Ϊ���ú����Ķ����򷵻�-2
				if (varName!=null&&varName.getIdentifier().equals(simpleName.getIdentifier())) {
//					System.out.println("������ú���");
					return -2;
				}
			}
			List<?> argumetns = methodInvocation.arguments();
			int index = 0;
			//��һ��������б��еĲ���
			for (Object object : argumetns) {
				astNode = (ASTNode)object;
				SimpleName varName = (SimpleName)getLeftVarName(astNode);
				if (varName!=null&&varName.getIdentifier().equals(simpleName.getIdentifier())) {
//					System.out.println("INDEX: "+index);
					return index;
				}
				index++;
			}
		}
		else if (methodInvoke instanceof SuperMethodInvocation) {
			SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation)methodInvoke;
			List<?> arguments = superMethodInvocation.arguments();
			int index = 0;
			ASTNode astNode;
			for (Object object : arguments) {
				astNode = (ASTNode)object;
				SimpleName varName = (SimpleName)getLeftVarName(astNode);
				if (varName!=null&&varName.getIdentifier().equals(simpleName.getIdentifier())) {
//					System.out.println("INDEX: "+index);
					return index;
				}
				index++;
			}
		}
		return -1;
	}
	
	/**
	 * ��ȡnode���ڵ�����
	 * @param node
	 * @return
	 */
	public String getClassName(ASTNode node) {
		if (node==null) {
			return null;
		}
		ASTNode parentNode = node.getParent();
		while(!(parentNode instanceof TypeDeclaration)&&parentNode!=node.getRoot()){
			parentNode = parentNode.getParent();
		}
		if (parentNode instanceof TypeDeclaration) {
			if (((TypeDeclaration) parentNode).resolveBinding()!=null&&((TypeDeclaration) parentNode).resolveBinding().getBinaryName()!=null) {
				return ((TypeDeclaration) parentNode).resolveBinding().getBinaryName();
			}
		}
		return null;
	}
	
	public TypeDeclaration getClassTypeDeclaration(ASTNode node) {
		if (node==null) {
			return null;
		}
		ASTNode parentNode = node.getParent();
		while(!(parentNode instanceof TypeDeclaration)&&parentNode!=node.getRoot()){
			parentNode = parentNode.getParent();
		}
		if (parentNode instanceof TypeDeclaration) {
			return (TypeDeclaration) parentNode;
		}
		return null;
	}
	
	/**
	 * ����Ƭ�Σ�����������
	 * @param variableDeclarationFragment
	 * @param typeDeclaration
	 * @return
	 */
	public int getFieldIndex(VariableDeclarationFragment variableDeclarationFragment,TypeDeclaration typeDeclaration) {
		int index = 0;
		if (variableDeclarationFragment!=null&&variableDeclarationFragment.resolveBinding()!=null&&typeDeclaration!=null&&typeDeclaration.resolveBinding()!=null) {
			IVariableBinding variableBinding = variableDeclarationFragment.resolveBinding();
			ITypeBinding typeBinding = typeDeclaration.resolveBinding();
			if (variableBinding.getDeclaringClass()!=null) {
				String fieldClassName = variableBinding.getDeclaringClass().getBinaryName();
				if (fieldClassName!=null) {
					while(true){
						if (typeBinding!=null) {
							//�ҵ�field���ڵ���
							if (fieldClassName.equals(typeBinding.getBinaryName())) {
								return index+variableBinding.getVariableId();
							}
							index += typeBinding.getDeclaredFields().length;
							typeBinding = typeBinding.getSuperclass();
						}
						else{
							return -1;
						}
					}
				}
			}
		}
		return -1;
	}
	
	public SimpleName getSecondName(ASTNode node) {
		if (node instanceof QualifiedName) {
			if (((QualifiedName)node).getQualifier() instanceof QualifiedName) {
				return getSecondName(((QualifiedName)node).getQualifier());
			}
			else {
				return ((QualifiedName)node).getName();
			}
		}
		return null;
	}
	
	/**
	 * ���ø��ຯ�����µĳ�Ա����������ƫ��
	 * @param locClass
	 * @param decClass
	 * @return
	 */
	public int getIndexOffset(ITypeBinding locClass,ITypeBinding decClass) {
		String decClassName = decClass.getBinaryName();
		int offset = 0;
		while(locClass!=null&&locClass.getBinaryName()!=null&&
				locClass.getDeclaredFields()!=null&&
				!locClass.getBinaryName().equals(decClassName)){
			offset += locClass.getDeclaredFields().length;
			locClass = locClass.getSuperclass();
		}
		if (locClass==null) {
			return -1;
		}
		return offset;
	}
	
	/**
	 * ��ȡ��Ա������index
	 * @param node
	 * @return
	 */
	public int getVarIndex(ASTNode node) {
		String decClassName = null;
		IVariableBinding variableBinding=null;
		if (node instanceof SimpleName) {
			SimpleName simpleName = (SimpleName)node;
			if (simpleName.resolveBinding() instanceof IVariableBinding) {
				variableBinding = (IVariableBinding) simpleName.resolveBinding();
				if (variableBinding.getDeclaringClass()!=null) {
					decClassName = variableBinding.getDeclaringClass().getBinaryName();
				}
			}
		}
		else if (node instanceof QualifiedName) {
			SimpleName simpleName = getSecondName(node);
			if (simpleName!=null&&simpleName.resolveBinding() instanceof IVariableBinding) {
				variableBinding = (IVariableBinding) simpleName.resolveBinding();
				if (variableBinding.getDeclaringClass()!=null) {
					decClassName = variableBinding.getDeclaringClass().getBinaryName();
				}
			}
		}
		int index = 0;
		TypeDeclaration typeDeclaration = getClassTypeDeclaration(node);
		if (typeDeclaration!=null) {
			ITypeBinding typeBinding = typeDeclaration.resolveBinding();
			if (decClassName!=null) {
				while(true){
					if (typeBinding!=null) {
						//�ҵ�field���ڵ���
						if (decClassName.equals(typeBinding.getBinaryName())) {
							return index+variableBinding.getVariableId();
						}
						index += typeBinding.getDeclaredFields().length;
						typeBinding = typeBinding.getSuperclass();
					}
					else{
						return -1;
					}
				}
			}
		}
		return -1;
	}
	
	/**
	 * ��ʾ���ú����ı������������-1��ʾû�иı䣬-2��ʾ��Ա�����ı䣬�����ʾ�ı������������0��1...��
	 * @param object
	 * @return
	 */
	public int getLeftNodeIndex(Object object) {
		if (object instanceof ASTNode) {
			ASTNode node = (ASTNode) object;
			ASTNode leftNode = getLeftVarName(node);
			if (leftNode instanceof SimpleName) {
				SimpleName name = (SimpleName) leftNode;
				if (name.resolveBinding() instanceof IVariableBinding) {
					IVariableBinding binding = (IVariableBinding) name.resolveBinding();
					if (binding.isField()) {  //�ı���һ����Ա����
						return -2;
					}
					else if (binding.isParameter()) {  //�ı���һ����������,����index
						return binding.getVariableId();
					}
				}
			}
		}
		return -1;
	}
	
}
