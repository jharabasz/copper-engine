/*
 * Copyright 2002-2013 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.scoopgmbh.copper.monitoring.client.form.filter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import com.sun.javafx.scene.control.behavior.TableCellBehavior;

import de.scoopgmbh.copper.monitoring.client.form.FxmlController;
import de.scoopgmbh.copper.monitoring.client.util.MessageProvider;

/**
*
* @param <F>Filtermodel
* @param <R>Resultmodel
*/
public abstract class FilterResultControllerBase<F,R> implements FilterResultController<F,R>, FxmlController {
	
	List<TableView<?>> tableViews = new ArrayList<TableView<?>>();
	public <M> HBox createTabelControlls(final TableView<M> tableView){
		this.tableViews.add(tableView);
		
		if (tableView.getContextMenu()==null){
			tableView.setContextMenu(new ContextMenu());
		}
		final MenuItem copyMenuItem = new MenuItem("copy table");
		final MenuItem copyCellMenuItem = new MenuItem("copy cell");
		fillContextMenu(tableView, copyMenuItem,copyCellMenuItem);
//		tableView.contextMenuProperty().addListener(new ChangeListener<ContextMenu>() {
//			@Override
//			public void changed(ObservableValue<? extends ContextMenu> observable, ContextMenu oldValue, ContextMenu newValue) {
//				if (newValue!=null){
//					fillContextMenu(tableView, copyMenuItem,copyCellMenuItem);
//				}
//			}
//		});
		
		final CheckBox regExp = new CheckBox("RegExp");
		
		HBox pane= new HBox();
		BorderPane.setMargin(pane,new Insets(3));
		pane.setSpacing(3);
		pane.setAlignment(Pos.CENTER_LEFT);
		Button copy = new Button("copy");
		copy.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				copyTable(tableView);
			}
		});
		copyMenuItem.setOnAction(copy.getOnAction());
		pane.getChildren().add(copy);
		Button copyCell = new Button("copy cell");
		copyCell.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				copyTableCell(tableView);
			}
		});
		copyCellMenuItem.setOnAction(copyCell.getOnAction());
		pane.getChildren().add(copyCell);
		pane.getChildren().add(new Label("Search"));
		final TextField textField = new TextField();
		textField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (newValue!=null && newValue.length()>1){
					searchInTable(tableView, newValue,regExp.isSelected());
				}
			}
		});
		regExp.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue!=null){
					searchInTable(tableView,textField.getText(),newValue);
				}
			}
		});
		textField.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				searchInTable(tableView,textField.getText(),regExp.isSelected());
			}
		});
		HBox.setHgrow(textField,Priority.ALWAYS);
		final Label count = new Label("count: 0");
//		tableView.itemsProperty().addListener(new ChangeListener<ObservableList<M>>() {
//			@Override
//			public void changed(ObservableValue<? extends ObservableList<M>> observable, ObservableList<M> oldValue,
//					ObservableList<M> newValue) {
//				if (newValue!=null){
//					count.setText("count: "+String.valueOf(newValue.size()));
//				}
//			}
//		});
		pane.getChildren().add(textField);
		pane.getChildren().add(regExp);
		pane.getChildren().add(new Separator(Orientation.VERTICAL));
		pane.getChildren().add(count);
		
		tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		return pane;
	}


	private <M> void fillContextMenu(final TableView<M> tableView, final MenuItem copyMenuItem, MenuItem copyCellMenuItem) {
		tableView.getContextMenu().getItems().add(new SeparatorMenuItem());
		tableView.getContextMenu().getItems().add(copyMenuItem);
		tableView.getContextMenu().getItems().add(copyCellMenuItem);
	}
	
	private void searchInTable(final TableView<?> tableView, String newValue, boolean useRegex) {
		tableView.getSelectionModel().clearSelection();
		if (useRegex){
			try {
				Pattern.compile(newValue);
			} catch (PatternSyntaxException e) {
				return;
			}
		}
		int selectedRow= tableView.getSelectionModel().getSelectedIndex();
		int toSelectedRow= tableView.getSelectionModel().getSelectedIndex();
		for (int row=0; row<tableView.getItems().size();row++){
			String rowString="";
			int rowIndex=(row+1+selectedRow)%tableView.getItems().size();
			for (int column=0; column<tableView.getColumns().size();column++){
				 Object cell = tableView.getColumns().get(column).getCellData(rowIndex);
				 if (cell!=null && cell.toString()!=null){
					 rowString+=cell.toString();
				 }
			}
			if (useRegex?rowString.matches(newValue):rowString.contains(newValue)){
				toSelectedRow=rowIndex;
				break;
			}
		}
		final int rowFinal = toSelectedRow;
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				tableView.getSelectionModel().select(rowFinal);
				tableView.getFocusModel().focus(rowFinal);
				tableView.scrollTo(rowFinal);
			}
		});
	}
	
	private void copyTable(final TableView<?> tableView) {
		StringBuilder clipboardString = new StringBuilder();
		for (int row=0; row<tableView.getItems().size();row++){
			for (int column=0; column<tableView.getColumns().size();column++){
				 Object cell = tableView.getColumns().get(column).getCellData(row);
				 clipboardString.append(cell);
                clipboardString.append("\t");
			}
			clipboardString.append("\n");
		}
        final ClipboardContent content = new ClipboardContent();
        content.putString(clipboardString.toString());
        Clipboard.getSystemClipboard().setContent(content);
	}
	
	private void copyTableCell(final TableView<?> tableView) {
		StringBuilder clipboardString = new StringBuilder();
		for (TablePosition<?, ?> tablePosition: tableView.getSelectionModel().getSelectedCells()){
			 Object cell = tableView.getColumns().get(tablePosition.getColumn()).getCellData(tablePosition.getRow());
			 clipboardString.append(cell);
		}
        final ClipboardContent content = new ClipboardContent();
        content.putString(clipboardString.toString());
        Clipboard.getSystemClipboard().setContent(content);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void onClose(){
		//workaround for javafx memoryleaks RT-25652, RT-32087
		for (TableView tableView: tableViews){
			tableView.getFocusModel().focus(null);
			Class tcbClass = TableCellBehavior.class;
			try{
				Method anchorMethod = tcbClass.getDeclaredMethod("setAnchor", TableView.class, TablePosition.class);
				anchorMethod.setAccessible(true);
				anchorMethod.invoke(null,  tableView, null);
			} catch (Exception e){
				throw new RuntimeException(e);
			}
			tableView.setOnMouseClicked(null);
			tableView.setSelectionModel(null);
			tableView.getColumns().clear();
			tableView.setItems(FXCollections.observableArrayList());
			tableView=null;
		}
		tableViews.clear();
		
	}
	
	@Override
	public boolean supportsClear() {
		return false;
	}

	@Override
	public void clear() {
		// default empty implementation		
	}
	
	@Override
	public List<? extends Node> getContributedButtons(MessageProvider messageProvider) {
		return Collections.emptyList();
	}
}
