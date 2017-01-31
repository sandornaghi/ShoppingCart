package com.annotations;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.elasticsearch.client.transport.TransportClient;

import com.encrypt.CodeDecodeTokens;

public class NotEmptyAdminValidator implements ConstraintValidator<NotEmptyAdmin, String> {

	@Override
	public void initialize(NotEmptyAdmin arg0) {

	}

	@Inject
	TransportClient transportClient;
	
	@Override
	public boolean isValid(String arg0, ConstraintValidatorContext arg1) {

		CodeDecodeTokens cdt = new CodeDecodeTokens();
		boolean haveRights = cdt.userIsAdmin(transportClient, arg0);
		if (!haveRights) {
			return false;
		} else {
			return true;
		}
	}
	
}
