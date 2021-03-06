/*
 * Copyright (c) 2009 tarchan
 * All rights reserved.
 * 
 * Distributed under the BSD Software License (see LICENSE.txt) 
 */
package com.mac.tarchan.desktop.event;

import java.awt.Button;
import java.awt.Component;
import java.awt.Container;
import java.awt.ItemSelectable;
import java.awt.TextComponent;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.TextListener;
import java.beans.EventHandler;
import java.beans.PropertyChangeListener;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.MenuElement;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mac.tarchan.desktop.DesktopSupport;

/**
 * EventQuery
 * 
 * @see <a href="http://semooh.jp/jquery/api/events/">Events - jQuery 日本語リファレンス</a>
 * @see EventHandler
 */
public class EventQuery
{
	/** ログ */
	private static final Log log = LogFactory.getLog(EventQuery.class);

	/** 親ノード */
	protected EventQuery parent;

	/** ターゲット */
	protected Collection<Component> list;

	/**
	 * 新しい EventQuery を作成します。
	 * 
	 * @param child コンポーネント
	 */
	public EventQuery(Component... child)
	{
		list = new HashSet<Component>(Arrays.asList(child));
	}

	/**
	 * 新しい EventQuery を作成します。
	 * 
	 * @param child コンポーネント
	 * @return アクションファクトリー
	 */
	public static EventQuery from(Component... child)
	{
		return new EventQuery(child);
	}

	/**
	 * 子要素の配列を返します。
	 * 
	 * @param parent 親要素
	 * @return 子要素の配列
	 */
	protected Component[] getComponents(Container parent)
	{
		if (parent instanceof MenuElement)
		{
			MenuElement[] sub = ((MenuElement)parent).getSubElements();
			Component[] com = new Component[sub.length];
			for (int i = 0; i < sub.length; i++)
			{
				com[i] = sub[i].getComponent();
			}
	
			return com;
		}
		else
		{
			return parent.getComponents();
		}
	}

	List<String> list(Collection<Component> list)
	{
		ArrayList<String> buf = new ArrayList<String>();
		for (Component child : list)
		{
			buf.add(String.format("%s (%s)", child.getName(), child.getClass().getName()));
		}

		return buf;
	}

	/**
	 * 指定された文字列にマッチするコンポーネントを検索します。
	 * 
	 * @param names 名前
	 * @return アクションファクトリー
	 * @see String#matches(String)
	 */
	public EventQuery find(String... names)
	{
		if (names == null) throw new IllegalArgumentException("names");

//		System.out.format("find: names=%s, list=(%s), parent=%s\n", Arrays.asList(names), list(list), parent);
		log.debug(String.format("find: names=%s, list=(%s), parent=%s", Arrays.asList(names), list(list), parent));
//		EventQuery action = EventQuery.ready(comp);
		EventQuery query = new EventQuery();
		query.parent = this;
		for (Component child : list)
		{
			for (String name : names)
			{
				query.find(child, name);
			}
		}
//		System.out.format("list: %s\n", list(query.list));

		return query;
	}

	/**
	 * 指定された文字列にマッチするコンポーネントを検索します。
	 * 
	 * @param base 親コンポーネント
	 * @param name 名前
	 * @return このオブジェクト
	 */
	protected EventQuery find(Component base, String name)
	{
		log.debug(String.format("find: name=%s, list=(%s), parent=%s", name, list(list), parent));
		if (base instanceof Container)
		{
			Container parent = (Container)base;
//			System.out.format("Container: %s\n", parent.getName());
			for (Component child : getComponents(parent))
			{
				find(child, name);
			}
		}

//		System.out.format("Component: %s [%s]\n", base.getName(),  base.getClass().getName());
		if (base.getName() != null && base.getName().matches(name))
		{
//			System.out.format("add list: %s [%s]\n", base.getName(),  name);
			list.add(base);
		}

		return this;
	}

	/**
	 * 指定されたタイプにマッチするコンポーネントを検索します。
	 * 
	 * @param types タイプ
	 * @return 新しい EventQuery オブジェクト
	 */
	public EventQuery find(Class<?>... types)
	{
		if (types == null) throw new IllegalArgumentException("types");

//		System.out.format("find: types=%s ,list=(%s), parent=%s\n", Arrays.asList(types), list(list), parent);
		log.debug(String.format("find: types=%s ,list=(%s), parent=%s", Arrays.asList(types), list(list), parent));
		EventQuery query = new EventQuery();
		query.parent = this;
		for (Component child : list)
		{
			for (Class<?> type : types)
			{
				query.find(child, type);
			}
		}
//		System.out.format("list: %s\n", list(query.list));
		log.debug(String.format("list: %s\n", list(query.list)));

		return query;
	}

	/**
	 * 指定されたタイプにマッチするコンポーネントを検索します。
	 * 
	 * @param base 親コンポーネント
	 * @param type タイプ
	 * @return 新しい EventQuery オブジェクト
	 */
	protected EventQuery find(Component base, Class<?> type)
	{
		if (base instanceof Container)
		{
			Container parent = (Container)base;
//			System.out.format("Container: %s [%s], %s\n", parent.getName(),  parent.getClass().getName(), Arrays.asList(getComponents(parent)));
			for (Component child : getComponents(parent))
			{
				find(child, type);
			}
		}

//		System.out.format("Component: %s [%s]\n", base.getName(),  base.getClass().getName());
		if (type.isInstance(base))
		{
//			System.out.format("add list: %s [%s]\n", base.getName(),  base.getClass().getName());
			list.add(base);
		}

		return this;
	}

	/**
	 * 最上位のコンポーネントを検索します。
	 * 
	 * @return 新しい EventQuery オブジェクト
	 */
	public EventQuery root()
	{
		Window root = DesktopSupport.componentOwner(list.iterator().next(), Window.class);
		EventQuery query = EventQuery.from(root);
		return query;
	}

	/**
	 * ボタンコンポーネントを検索します。
	 * 
	 * @return 新しい EventQuery オブジェクト
	 * @see #find(Class...)
	 * @see AbstractButton
	 */
	public EventQuery button()
	{
		return find(AbstractButton.class);
	}

	/**
	 * インプットコンポーネントを検索します。
	 * 
	 * @return 新しい EventQuery オブジェクト
	 * @see #find(Class...)
	 * @see JTextComponent
	 */
	public EventQuery input()
	{
		return find(JTextComponent.class);
	}

	/**
	 * 親の EventQuery を返します。
	 * 
	 * @return 親の EventQuery
	 */
	public EventQuery end()
	{
		return parent;
	}

	/**
	 * 現在のコンポーネントのリストを出力します。
	 * 
	 * @param out 出力ストリーム
	 * @return このオブジェクト
	 */
	public EventQuery dump(PrintStream out)
	{
		String head = String.format("Parent: %s", parent);
		out.println(head);
		for (Component child : list)
		{
			String info = String.format("%s, %s", child.getName(), child.getClass().getName());
			out.println(info);
		}

		return this;
	}

	/**
	 * クリックイベントのハンドラを登録します。
	 * 
	 * @param actionPerformed ハンドラ
	 * @return このオブジェクト
	 * @see ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public EventQuery click(ActionListener actionPerformed)
	{
		for (Component child : list)
		{
			if (AbstractButton.class.isInstance(child))
			{
				AbstractButton.class.cast(child).addActionListener(actionPerformed);
			}
			else if (Button.class.isInstance(child))
			{
				Button.class.cast(child).addActionListener(actionPerformed);
			}
			else if (JTextField.class.isInstance(child))
			{
				JTextField.class.cast(child).addActionListener(actionPerformed);
			}
			else
			{
				// 何もしない
			}
		}
		return this;
	}

	/**
	 * クリックイベントのハンドラを登録します。
	 * 
	 * @param target アクションを実行するオブジェクト
	 * @param action ターゲット上の書き込み可能なプロパティまたはメソッドの名前
	 * @param property 受信イベントの読み込み可能なプロパティの完全指定された名前 
	 * @return このオブジェクト
	 * @see #click(ActionListener)
	 */
	public EventQuery click(Object target, String action, String property)
	{
//		System.out.format("target: %s [%s]\n", target.getClass().getName(), action);
		ActionListener actionPerformed = EventHandler.create(ActionListener.class, target, action, property, "actionPerformed");
//		MouseListener mouseClicked = EventHandler.create(MouseListener.class, target, action, property, "mouseClicked");
//		click(handler);
//		for (Component child : list)
//		{
////			System.out.format("click: %s [%s]\n", child.getName(), child.getClass().getName());
//			if (child instanceof AbstractButton)
//			{
//				((AbstractButton)child).addActionListener(actionPerformed);
//			}
//			else if (child instanceof Button)
//			{
//				((Button)child).addActionListener(actionPerformed);
//			}
//			else if (child instanceof JTextField)
//			{
//				((JTextField)child).addActionListener(actionPerformed);
//			}
//			else
//			{
//				child.addMouseListener(mouseClicked);
//			}
//		}
//
//		return this;
		return click(actionPerformed);
	}

	/**
	 * クリックイベントのハンドラを登録します。
	 * 
	 * @param target アクションを実行するオブジェクト
	 * @param action ターゲット上の書き込み可能なプロパティまたはメソッドの名前
	 * @return このオブジェクト
	 * @see #click(Object, String, String)
	 */
	public EventQuery click(Object target, String action)
	{
		return click(target, action, null);
	}

	/**
	 * クリックイベントのハンドラを登録します。
	 * 
	 * @param target アクションを実行するオブジェクト
	 * @return このオブジェクト
	 * @see #click(Object, String)
	 */
	public EventQuery click(Object target)
	{
//		System.out.println("click: " + list(list));
		for (Component child : list)
		{
			String action = child.getName();
			if (action != null) find(action).click(target, action);
		}
		return this;
	}

	/**
	 * ダブルクリックのハンドラを登録します。
	 * 
	 * @param mouseClicked ハンドラ
	 * @return このオブジェクト
	 * @see MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public EventQuery dblclick(final MouseListener mouseClicked)
	{
		log.debug("dblclick=" + list);
		for (Component child : list)
		{
			child.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseClicked(MouseEvent e)
				{
					if (e.getClickCount() == 2) mouseClicked.mouseClicked(e);
				}
			});
		}
		return this;
	}

	/**
	 * ダブルクリックのハンドラを登録します。
	 * 
	 * @param target アクションを実行するオブジェクト
	 * @param action ターゲット上の書き込み可能なプロパティまたはメソッドの名前
	 * @param property 受信イベントの読み込み可能なプロパティの完全指定された名前 
	 * @return このオブジェクト
	 * @see MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public EventQuery dblclick(Object target, String action, String property)
	{
		MouseListener mouseClicked = EventHandler.create(MouseListener.class, target, action, property, "mouseClicked");
//		DoubleClickHandler dblclickHandler = new DoubleClickHandler(mouseClicked);
//		for (Component child : list)
//		{
//			child.addMouseListener(dblclickHandler);
//		}
//
//		return this;
		return dblclick(mouseClicked);
	}

	/**
	 * ダブルクリックのハンドラを登録します。
	 * 
	 * @param target アクションを実行するオブジェクト
	 * @param action ターゲット上の書き込み可能なプロパティまたはメソッドの名前
	 * @return このオブジェクト
	 * @see #dblclick(Object, String, String)
	 */
	public EventQuery dblclick(Object target, String action)
	{
		return dblclick(target, action, null);
	}

	/**
	 * ダブルクリックのハンドラを登録します。
	 * 
	 * @param target アクションを実行するオブジェクト
	 * @return このオブジェクト
	 * @see #dblclick(Object, String)
	 */
	public EventQuery dblclick(Object target)
	{
		for (Component child : list)
		{
			String action = child.getName();
			if (action != null) find(action).dblclick(target, action);
		}
		return this;
	}

	/**
	 * フォーカスがリクエスト可能になったタイミングでハンドラを実行します。
	 * 
	 * @param hierarchyChanged ハンドラ
	 * @return このオブジェクト
	 * @see <a href="http://terai.xrea.jp/Swing/DefaultFocus.html ">Windowを開いたときのフォーカスを指定 - Java Swing Tips</a>
	 * @see HierarchyListener#hierarchyChanged(HierarchyEvent)
	 */
	public EventQuery ready(final HierarchyListener hierarchyChanged)
	{
		for (Component child : list)
		{
			child.addHierarchyListener(new HierarchyListener()
			{
				public void hierarchyChanged(HierarchyEvent e)
				{
					if (e.getChangeFlags() == HierarchyEvent.DISPLAYABILITY_CHANGED) hierarchyChanged.hierarchyChanged(e);
				}
			});
		}
		return this;
	}

	/**
	 * フォーカスがリクエスト可能になったタイミングでハンドラを実行します。
	 * 
	 * @param target アクションを実行するオブジェクト
	 * @param action ターゲット上の書き込み可能なプロパティまたはメソッドの名前
	 * @param property 受信イベントの読み込み可能なプロパティの完全指定された名前 
	 * @return このオブジェクト
	 * @see HierarchyListener#hierarchyChanged(HierarchyEvent)
	 * @see HierarchyEvent
	 */
	public EventQuery ready(Object target, String action, String property)
	{
		HierarchyListener hierarchyChanged = EventHandler.create(HierarchyListener.class, target, action, property, "hierarchyChanged");
		return ready(hierarchyChanged);
	}

	/**
	 * コンポーネントの表示／非表示を切り替えます。
	 */
	public void toggle()
	{
		for (Component child : list)
		{
			child.setVisible(!child.isVisible());
		}
	}

	/**
	 * コンポーネントを表示します。
	 */
	public void show()
	{
		for (Component child : list)
		{
			child.setVisible(true);
		}
	}

	/**
	 * コンポーネント表示イベントのハンドラを登録します。
	 * 
	 * @param target アクションを実行するオブジェクト
	 * @param action ターゲット上の書き込み可能なプロパティまたはメソッドの名前
	 * @param property 受信イベントの読み込み可能なプロパティの完全指定された名前 
	 * @return このオブジェクト
	 * @see ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	public EventQuery show(Object target, String action, String property)
	{
		ComponentListener componentShown = EventHandler.create(ComponentListener.class, target, action, property, "componentShown");
		for (Component child : list)
		{
			child.addComponentListener(componentShown);
		}
		return this;
	}

	/**
	 * コンポーネント表示イベントのハンドラを登録します。
	 * 
	 * @param target アクションを実行するオブジェクト
	 * @param action ターゲット上の書き込み可能なプロパティまたはメソッドの名前
	 * @return このオブジェクト
	 * @see #show(Object, String, String)
	 */
	public EventQuery show(Object target, String action)
	{
		return show(target, action, null);
	}

	/**
	 * コンポーネントを隠します。
	 */
	public void hide()
	{
		for (Component child : list)
		{
			child.setVisible(false);
		}
	}

	/**
	 * コンポーネント不可視イベントのハンドラを登録します。
	 * 
	 * @param target アクションを実行するオブジェクト
	 * @param action ターゲット上の書き込み可能なプロパティまたはメソッドの名前
	 * @param property 受信イベントの読み込み可能なプロパティの完全指定された名前 
	 * @return このオブジェクト
	 * @see ComponentListener#componentHidden(java.awt.event.ComponentEvent)
	 */
	public EventQuery hide(Object target, String action, String property)
	{
		ComponentListener componentHidden = EventHandler.create(ComponentListener.class, target, action, property, "componentHidden");
		for (Component child : list)
		{
			child.addComponentListener(componentHidden);
		}
		return this;
	}

	/**
	 * コンポーネント不可視イベントのハンドラを登録します。
	 * 
	 * @param target アクションを実行するオブジェクト
	 * @param action ターゲット上の書き込み可能なプロパティまたはメソッドの名前
	 * @return このオブジェクト
	 * @see #hide(Object, String, String)
	 */
	public EventQuery hide(Object target, String action)
	{
		return hide(target, action, null);
	}

	/**
	 * コンポーネントのサイズが変更されたとき
	 * 
	 * @param target アクションを実行するオブジェクト
	 * @param action ターゲット上の書き込み可能なプロパティまたはメソッドの名前
	 * @param property 受信イベントの読み込み可能なプロパティの完全指定された名前 
	 * @return このオブジェクト
	 * @see ComponentListener#componentResized(java.awt.event.ComponentEvent)
	 */
	public EventQuery resize(Object target, String action, String property)
	{
		ComponentListener componentResized = EventHandler.create(ComponentListener.class, target, action, property, "componentResized");
		for (Component child : list)
		{
			child.addComponentListener(componentResized);
		}
		return this;
	}

	/**
	 * コンポーネントが移動されたとき
	 * 
	 * @param target アクションを実行するオブジェクト
	 * @param action ターゲット上の書き込み可能なプロパティまたはメソッドの名前
	 * @param property 受信イベントの読み込み可能なプロパティの完全指定された名前 
	 * @return このオブジェクト
	 * @see ComponentListener#componentMoved(java.awt.event.ComponentEvent)
	 */
	public EventQuery move(Object target, String action, String property)
	{
		ComponentListener componentMoved = EventHandler.create(ComponentListener.class, target, action, property, "componentMoved");
		for (Component child : list)
		{
			child.addComponentListener(componentMoved);
		}
		return this;
	}

	/**
	 * コンポーネント上にマウスカーソルが乗ったときと、外れたときにイベントハンドラを実行します。
	 * 
	 * @param target アクションを実行するオブジェクト
	 * @param overAction マウスカーソルが乗ったときの、ターゲット上の書き込み可能なプロパティまたはメソッドの名前
	 * @param outAction マウスカーソルが外れたときの、ターゲット上の書き込み可能なプロパティまたはメソッドの名前
	 * @param property 受信イベントの読み込み可能なプロパティの完全指定された名前 
	 * @return このオブジェクト
	 * @see MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 * @see MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public EventQuery hover(Object target, String overAction, String outAction, String property)
	{
		MouseListener mouseEntered = EventHandler.create(MouseListener.class, target, overAction, property, "mouseEntered");
		MouseListener mouseExited = EventHandler.create(MouseListener.class, target, outAction, property, "mouseExited");
		for (Component child : list)
		{
			child.addMouseListener(mouseEntered);
			child.addMouseListener(mouseExited);
		}
		return this;
	}

	/**
	 * コンポーネント上にマウスカーソルが乗ったときと、外れたときにイベントハンドラを実行します。
	 * 
	 * @param target アクションを実行するオブジェクト
	 * @param overAction マウスカーソルが乗ったときの、ターゲット上の書き込み可能なプロパティまたはメソッドの名前
	 * @param outAction マウスカーソルが外れたときの、ターゲット上の書き込み可能なプロパティまたはメソッドの名前
	 * @return このオブジェクト
	 * @see #hover(Object, String, String, String)
	 */
	public EventQuery hover(Object target, String overAction, String outAction)
	{
		return hover(target, overAction, outAction, null);
	}

	/**
	 * フォーカスを受け取ったとき
	 * 
	 * @param target アクションを実行するオブジェクト
	 * @param action ターゲット上の書き込み可能なプロパティまたはメソッドの名前
	 * @param property 受信イベントの読み込み可能なプロパティの完全指定された名前 
	 * @return このオブジェクト
	 * @see FocusListener#focusGained(java.awt.event.FocusEvent)
	 */
	public EventQuery focus(Object target, String action, String property)
	{
		FocusListener focusGained = EventHandler.create(FocusListener.class, target, action, property, "focusGained");
		for (Component child : list)
		{
			child.addFocusListener(focusGained);
		}
		return this;
	}

	/**
	 * コンポーネントがフォーカスを失ったときのイベントハンドラを登録します。
	 * 
	 * @param target アクションを実行するオブジェクト
	 * @param action ターゲット上の書き込み可能なプロパティまたはメソッドの名前
	 * @param property 受信イベントの読み込み可能なプロパティの完全指定された名前 
	 * @return このオブジェクト
	 * @see FocusListener#focusLost(java.awt.event.FocusEvent)
	 */
	public EventQuery blur(Object target, String action, String property)
	{
		FocusListener focusLost = EventHandler.create(FocusListener.class, target, action, property, "focusLost");
		for (Component child : list)
		{
			child.addFocusListener(focusLost);
		}
		return this;
	}

	/**
	 * キーを押しているとき
	 * 
	 * @param target アクションを実行するオブジェクト
	 * @param action ターゲット上の書き込み可能なプロパティまたはメソッドの名前
	 * @param property 受信イベントの読み込み可能なプロパティの完全指定された名前 
	 * @return このオブジェクト
	 * @see KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public EventQuery keydown(Object target, String action, String property)
	{
		KeyListener keyPressed = EventHandler.create(KeyListener.class, target, action, property, "keyPressed");
		for (Component child : list)
		{
			child.addKeyListener(keyPressed);
		}
		return this;
	}

	/**
	 * キーを離したとき
	 * 
	 * @param target アクションを実行するオブジェクト
	 * @param action ターゲット上の書き込み可能なプロパティまたはメソッドの名前
	 * @param property 受信イベントの読み込み可能なプロパティの完全指定された名前 
	 * @return このオブジェクト
	 * @see KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public EventQuery keyup(Object target, String action, String property)
	{
		KeyListener keyReleased = EventHandler.create(KeyListener.class, target, action, property, "keyReleased");
		for (Component child : list)
		{
			child.addKeyListener(keyReleased);
		}
		return this;
	}

	/**
	 * キーをタイプしたとき
	 * 
	 * @param target アクションを実行するオブジェクト
	 * @param action ターゲット上の書き込み可能なプロパティまたはメソッドの名前
	 * @param property 受信イベントの読み込み可能なプロパティの完全指定された名前 
	 * @return このオブジェクト
	 * @see KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public EventQuery keypress(Object target, String action, String property)
	{
		KeyListener keyTyped = EventHandler.create(KeyListener.class, target, action, property, "keyTyped");
		for (Component child : list)
		{
			child.addKeyListener(keyTyped);
		}
		return this;
	}

	/**
	 * 入力コンポーネントの値の変更が完了したときのイベントハンドラを登録します。
	 * リストの選択範囲が変更されたとき
	 * 
	 * @param target アクションを実行するオブジェクト
	 * @param action ターゲット上の書き込み可能なプロパティまたはメソッドの名前
	 * @param property 受信イベントの読み込み可能なプロパティの完全指定された名前 
	 * @return このオブジェクト
	 * @see TextListener#textValueChanged(java.awt.event.TextEvent)
	 * @see ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 * @see PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 * @see ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public EventQuery change(Object target, String action, String property)
	{
		TextListener textValueChanged = EventHandler.create(TextListener.class, target, action, property, "textValueChanged");
		ListSelectionListener valueChanged = EventHandler.create(ListSelectionListener.class, target, action, property, "valueChanged");
		PropertyChangeListener propertyChange = EventHandler.create(PropertyChangeListener.class, target, action, property, "propertyChange");
		ChangeListener stateChanged = EventHandler.create(ChangeListener.class, target, action, property, "stateChanged");
		for (Component child : list)
		{
			if (TextComponent.class.isInstance(child))
			{
				TextComponent.class.cast(child).addTextListener(textValueChanged);
			}
			else if (JTable.class.isInstance(child))
			{
				JTable.class.cast(child).getSelectionModel().addListSelectionListener(valueChanged);
			}
			else if (JTabbedPane.class.isInstance(child))
			{
				JTabbedPane.class.cast(child).addChangeListener(stateChanged);
			}
			else
			{
				child.addPropertyChangeListener(propertyChange);
			}
		}

		return this;
	}

	/**
	 * 入力コンポーネントの値の変更が完了したときのイベントハンドラを登録します。
	 * リストの選択範囲が変更されたとき
	 * 
	 * @param target アクションを実行するオブジェクト
	 * @param action ターゲット上の書き込み可能なプロパティまたはメソッドの名前
	 * @return このオブジェクト
	 * @see #change(Object, String, String)
	 */
	public EventQuery change(Object target, String action)
	{
		return change(target, action, null);
	}

	/**
	 * バウンドプロパティの変更時に呼び出されます。
	 * 
	 * @param name 待機しているプロパティの名前。null の場合は、すべてのプロパティを待機します。
	 * @param target アクションを実行するオブジェクト
	 * @param action ターゲット上の書き込み可能なプロパティまたはメソッドの名前
	 * @param property 受信イベントの読み込み可能なプロパティの完全指定された名前 
	 * @return このオブジェクト
	 * @see PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public EventQuery change(String name, Object target, String action, String property)
	{
		PropertyChangeListener propertyChange = EventHandler.create(PropertyChangeListener.class, target, action, property, "propertyChange");
		for (Component child : list)
		{
			if (name != null)
			{
				child.addPropertyChangeListener(name, propertyChange);
			}
			else
			{
				child.addPropertyChangeListener(propertyChange);
			}
		}

		return this;
	}

	/**
	 * ページをめくるような動作をしたとき
	 * 
	 * @param target アクションを実行するオブジェクト
	 * @param action ターゲット上の書き込み可能なプロパティまたはメソッドの名前
	 * @param property 受信イベントの読み込み可能なプロパティの完全指定された名前 
	 * @return このオブジェクト
	 * @see MouseListener#mousePressed(java.awt.event.MouseEvent)
	 * @see MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public EventQuery swipe(Object target, String action, String property)
	{
//		MouseListener mousePressed = EventHandler.create(MouseListener.class, target, action, property, "mousePressed");
		MouseMotionListener mouseDragged = EventHandler.create(MouseMotionListener.class, target, action, property, "mouseDragged");
		MouseSwipeHandler swipeHandler = new MouseSwipeHandler();
		for (Component child : list)
		{
//			child.addMouseListener(mousePressed);
			child.addMouseMotionListener(mouseDragged);
			child.addMouseListener(swipeHandler);
			child.addMouseMotionListener(swipeHandler);
		}

		return this;
	}

	/**
	 * テキストを設定します。
	 * 
	 * @param str テキスト
	 */
	public void text(String str)
	{
		for (Component child : list)
		{
			if (TextComponent.class.isInstance(child))
			{
				TextComponent.class.cast(child).setText(str);
			}
			else if (JTextComponent.class.isInstance(child))
			{
				JTextComponent.class.cast(child).setText(str);
			}
			else
			{
				// 何もしない
			}
		}
	}

	/**
	 * テキストを連結して返します。
	 * 
	 * @return テキスト
	 */
	public String text()
	{
		StringBuilder text = new StringBuilder();
		for (Component child : list)
		{
			if (TextComponent.class.isInstance(child))
			{
				text.append(TextComponent.class.cast(child).getText());
			}
			else if (JTextComponent.class.isInstance(child))
			{
				text.append(JTextComponent.class.cast(child).getText());
			}
			else
			{
				// 何もしない
			}
		}

		return text.toString();
	}

	/**
	 * int に変換します。
	 * 
	 * @param def int に変換できない場合の値
	 * @return int に変換した値
	 */
	public int toInt(int def)
	{
		try
		{
			String input = text();
			int val = Integer.parseInt(input);
			return val;
		}
		catch (NumberFormatException x)
		{
			return def;
		}
	}

	/**
	 * 値を取得します。
	 * 
	 * @return 値のマップ
	 */
	public Map<String, String> values()
	{
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		for (Component child : list)
		{
			String name = child.getName();
			if (name == null) continue;

			if (child instanceof TextComponent)
			{
				String value = ((TextComponent)child).getText();
				map.put(name, value);
			}
			else if (child instanceof JTextComponent)
			{
				String value = ((JTextComponent)child).getText();
				map.put(name, value);
			}
			else if (child instanceof JToggleButton)
			{
				boolean value = ((JToggleButton)child).isSelected();
				map.put(name, String.valueOf(value));
			}
			else
			{
//				String value = child.toString();
//				map.put(name, value != null ? value : "");
			}
		}

		return map;
	}

	/**
	 * 最初のコンポーネントを返します。
	 * 
	 * @param <T> コンポーネントの型
	 * @return コンポーネント
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public <T extends Component> T first()
	{
		Iterator<Component> it = list.iterator();
		return it.hasNext() ? (T)it.next() : null;
	}

	/**
	 * 最初のコンポーネントを返します。
	 * 
	 * @param type コンポーネントの型
	 * @return コンポーネント
	 */
	public <T extends Component> T first(Class<T> type)
	{
		Iterator<Component> it = list.iterator();
		return it.hasNext() ? type.cast(it.next()) : null;
	}

	/**
	 * コンポーネントの配列を返します。
	 * 
	 * @deprecated {@link #list(Class)} を使用します。
	 * @param <T> コンポーネントの型
	 * @return コンポーネントの配列
	 */
	@SuppressWarnings("unchecked")
	public <T> T[] list()
	{
		ArrayList<T> sublist = new ArrayList<T>();
		for (Component child : list)
		{
			try
			{
				sublist.add((T)child);
			}
			catch (Exception x)
			{
				// 何もしない
			}
		}

		return (T[])sublist.toArray();
	}

	/**
	 * 指定された型に一致するコンポーネントのリストを返します。
	 * 
	 * @param type コンポーネントの型
	 * @return コンポーネントの配列
	 */
	public <T> List<T> list(Class<T> type)
	{
		ArrayList<T> sublist = new ArrayList<T>();
		for (Component child : list)
		{
			if (type.isInstance(child))
			{
				sublist.add(type.cast(child));
			}
		}

		return sublist;
	}

	/**
	 * 選択されたコンポーネントを返します。
	 * 
	 * @return コンポーネント
	 */
	public int[] selected()
	{
		for (Component child : list)
		{
			if (child instanceof JTable)
			{
				JTable table = ((JTable)child);
				return table.getSelectedRows();
			}
		}

		return new int[0];
	}

	/**
	 * 選択された項目を返します。
	 * 項目が選択されない場合は、空の配列を返します。
	 * 
	 * @return 選択された項目
	 * @see JTable#getValueAt(int, int)
	 * @see ItemSelectable#getSelectedObjects()
	 */
	public Object[] selectedObjects()
	{
		for (Component child : list)
		{
			if (child instanceof JTable)
			{
				JTable table = ((JTable)child);
				int[] rows = table.getSelectedRows();
				int col = table.getSelectedColumn();
				ArrayList<Object> sublist = new ArrayList<Object>();
				for (int index : rows)
				{
					sublist.add(table.getValueAt(index, col));
				}
				return sublist.toArray();
			}
			else if (child instanceof ItemSelectable)
			{
				ItemSelectable selector = (ItemSelectable)child;
				return selector.getSelectedObjects();
			}
			else
			{
				// ignore
			}
		}

		return new Object[0];
	}

	/**
	 * 選択された項目があるかどうか判定します。
	 * 
	 * @return 選択された項目がある場合は true
	 */
	public boolean checked()
	{
		for (Component child : list)
		{
			if (child instanceof ItemSelectable)
			{
				ItemSelectable itemSelectable = ((ItemSelectable)child);
				Object[] selected = itemSelectable.getSelectedObjects();
//				System.out.println("checked: " + (selected != null ? Arrays.asList(selected) : null));
				if (selected != null) return true;
			}
		}

		return false;
	}
}
