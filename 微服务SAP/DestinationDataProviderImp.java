package com.leedarson.sap.sapClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;

public class DestinationDataProviderImp implements DestinationDataProvider {

	@SuppressWarnings("rawtypes")
	private Map provider = new HashMap();

	@SuppressWarnings("unchecked")
	public void addDestinationProperties(String destName, Properties props) {
		provider.put(destName, props);
	}

	@Override
	public Properties getDestinationProperties(String destName) {
		if (destName == null) {
			throw new NullPointerException("Destinantion name is empty.");
		}

		if (provider.size() == 0) {
			throw new IllegalStateException("Data provider is empty.");
		}

		return (Properties) provider.get(destName);
	}

	@Override
	public void setDestinationDataEventListener(DestinationDataEventListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean supportsEvents() {
		return false;
	}

}
