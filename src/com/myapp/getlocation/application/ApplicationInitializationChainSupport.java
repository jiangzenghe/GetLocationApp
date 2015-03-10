package com.myapp.getlocation.application;

import android.content.Context;

public abstract class ApplicationInitializationChainSupport implements
		ApplicationInitializationChain {

	private ApplicationInitializationChain previous;
	private ApplicationInitializationChain next;

	@Override
	public ApplicationInitializationChain getPrevious() {
		return previous;
	}

	@Override
	public void setPrevious(ApplicationInitializationChain previous) {
		if (previous != null) {
			if (previous.equals(this.previous)) {
				return ;
			}
			
			ApplicationInitializationChain oldValue = this.previous;
			this.previous = null;
			if (oldValue != null) {
				oldValue.setNext(null);
			}
			
			this.previous = previous;
			this.previous.setNext(this);
		}
		else {
			if (this.previous == null) {
				return ;
			}
			
			ApplicationInitializationChain oldValue = this.previous;
			this.previous = previous;
			if (oldValue != null) {
				oldValue.setNext(null);
			}
		}
	}

	@Override
	public ApplicationInitializationChain getNext() {
		return next;
	}

	@Override
	public void setNext(ApplicationInitializationChain next) {
		if (next != null) {
			if (next.equals(this.next)) {
				return ;
			}
			
			ApplicationInitializationChain oldValue = this.next;
			this.next = null;
			if (oldValue != null) {
				oldValue.setPrevious(null);
			}
			
			this.next = next;
			this.next.setPrevious(this);
		}
		else {
			if (this.next == null) {
				return;
			}
			
			ApplicationInitializationChain oldValue = this.next;
			this.next = next;
			if (oldValue != null) {
				oldValue.setPrevious(null);
			}
		}
	}
	
	protected void doNext(Context context) {
		if (getNext() != null) {
			getNext().doProcess(context);
		}
	}

}
