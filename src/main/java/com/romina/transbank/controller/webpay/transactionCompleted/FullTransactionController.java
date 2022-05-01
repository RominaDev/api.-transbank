package com.romina.transbank.controller.webpay.transactionCompleted;

import cl.transbank.common.IntegrationApiKeys;
import cl.transbank.common.IntegrationCommerceCodes;
import cl.transbank.common.IntegrationType;

import cl.transbank.webpay.common.WebpayOptions;
import cl.transbank.webpay.exception.*;
import cl.transbank.webpay.transaccioncompleta.FullTransaction;
import cl.transbank.webpay.transaccioncompleta.responses.FullTransactionCommitResponse;
import cl.transbank.webpay.transaccioncompleta.responses.FullTransactionCreateResponse;
import cl.transbank.webpay.transaccioncompleta.responses.FullTransactionInstallmentResponse;
import cl.transbank.webpay.transaccioncompleta.responses.FullTransactionRefundResponse;
import cl.transbank.webpay.transaccioncompleta.responses.FullTransactionStatusResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.romina.transbank.controller.webpay.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

@Controller
@RequestMapping(value = "/fulltransaction")
public class FullTransactionController extends BaseController {
	private static Logger log = LoggerFactory.getLogger(FullTransactionController.class);

	@RequestMapping(value = "/create", method = RequestMethod.GET)
	public ModelAndView fullTransactionCreate(HttpServletRequest request) {

		log.info("Transaccion completa FullTransaction.create");
		String buyOrder = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));
		String sessionId = String.valueOf(new Random().nextInt(Integer.MAX_VALUE));
		double amount = 10000;
		String cardNumber = "4051885600446623";
		String cardExpirationDate = "23/03";
		short cvv = 123;

		Map<String, Object> details = new HashMap<>();
		details.put("buyOrder", buyOrder);
		details.put("sessionId", sessionId);
		details.put("amount", amount);
		details.put("cardNumber", cardNumber);
		details.put("cardExpirationDate", cardExpirationDate);
		details.put("cvv", cvv);

		try {
			FullTransaction tx = new FullTransaction(new WebpayOptions(IntegrationCommerceCodes.TRANSACCION_COMPLETA,
					IntegrationApiKeys.WEBPAY, IntegrationType.TEST));
			FullTransactionCreateResponse response = tx.create(buyOrder, sessionId, amount, cvv, cardNumber,
					cardExpirationDate);
			details.put("token", response.getToken());
		} catch (TransactionCreateException | IOException e) {
			log.error(e.getLocalizedMessage(), e);
			return new ErrorController().error();
		}

		return new ModelAndView("transaccioncompleta/transaction-create", "details", details);
	}

	@RequestMapping(value = { "/commit" }, method = RequestMethod.POST)
	public ModelAndView commit(@RequestParam("token") String tokenWs, HttpServletRequest request,
			@RequestParam("idQueryInstallments") Long idQueryInstallments) {
		log.info(String.format("token_ws : %s", tokenWs));

		byte deferredPeriodIndex = 1;
		Boolean gracePeriod = false;

		Map<String, Object> details = new HashMap<>();
		details.put("token_ws", tokenWs);
		details.put("deferred_period_index", deferredPeriodIndex);
		details.put("grace_period", gracePeriod);

		try {
			FullTransaction tx = new FullTransaction(new WebpayOptions(IntegrationCommerceCodes.TRANSACCION_COMPLETA,
					IntegrationApiKeys.WEBPAY, IntegrationType.TEST));
			final FullTransactionCommitResponse response = tx.commit(tokenWs, idQueryInstallments, deferredPeriodIndex,
					gracePeriod);

			details.put("amount", response.getAmount());
			log.debug(String.format("response : %s", response));
			details.put("response", response);
		} catch (TransactionCommitException | IOException e) {
			log.error(e.getLocalizedMessage(), e);
			return new ErrorController().error();
		}

		return new ModelAndView("transaccioncompleta/transaction-commit", "details", details);
	}

	@RequestMapping(value = { "/commit-without-installments" }, method = RequestMethod.POST)
	public ModelAndView commitWithoutInstallments(@RequestParam("token") String tokenWs, HttpServletRequest request) {
		log.info(String.format("token_ws : %s", tokenWs));

		Boolean gracePeriod = false;

		Map<String, Object> details = new HashMap<>();
		details.put("token_ws", tokenWs);
		details.put("grace_period", gracePeriod);
		try {
			FullTransaction tx = new FullTransaction(new WebpayOptions(IntegrationCommerceCodes.TRANSACCION_COMPLETA,
					IntegrationApiKeys.WEBPAY, IntegrationType.TEST));
			final FullTransactionCommitResponse response = tx.commit(tokenWs, null, null, gracePeriod);

			details.put("amount", response.getAmount());
			log.debug(String.format("response : %s", response));
			details.put("response", response);
		} catch (TransactionCommitException | IOException e) {
			log.error(e.getLocalizedMessage(), e);
			return new ErrorController().error();
		}

		return new ModelAndView("transaccioncompleta/transaction-commit", "details", details);
	}

	@RequestMapping(value = "/installments", method = RequestMethod.POST)
	public ModelAndView refund(@RequestParam("token") String token,
			@RequestParam("installmentsNumber") byte installmentsNumber) {

		cleanModel();
		addRequest("token", token);
		addRequest("installmentsNumber", installmentsNumber);
		System.out.println("Installments Number:" + installmentsNumber);

		try {
			// Versión 3.x del SDK
			FullTransaction tx = new FullTransaction(new WebpayOptions(IntegrationCommerceCodes.TRANSACCION_COMPLETA, IntegrationApiKeys.WEBPAY, IntegrationType.TEST));
			final FullTransactionInstallmentResponse response = tx.installments(token, installmentsNumber);

				addModel("response", response);
			addModel("token", token);
		} catch (IOException | TransactionInstallmentException e) {
			log.error(e.getLocalizedMessage(), e);
			return new ErrorController().error();
		}

		return new ModelAndView("transaccioncompleta/transaction-installments", "model", getModel());
	}

	@RequestMapping(value = { "/status" }, method = RequestMethod.POST)
	public ModelAndView status(@RequestParam("token") String tokenWs, HttpServletRequest request) {
		log.info(String.format("token_ws : %s", tokenWs));

		Map<String, Object> details = new HashMap<>();
		details.put("token_ws", tokenWs);

		try {

			FullTransaction tx = new FullTransaction(new WebpayOptions(IntegrationCommerceCodes.TRANSACCION_COMPLETA,
					IntegrationApiKeys.WEBPAY, IntegrationType.TEST));
			final FullTransactionStatusResponse response = tx.status(tokenWs);

			details.put("amount", response.getAmount());
			log.debug(String.format("response : %s", response));
			details.put("response", response);
		} catch (TransactionStatusException | IOException e) {
			log.error(e.getLocalizedMessage(), e);
			return new ErrorController().error();
		}

		return new ModelAndView("transaccioncompleta/transaction-status", "details", details);
	}

	@RequestMapping(value = "/status-form", method = RequestMethod.GET)
	public ModelAndView statusForm(HttpServletRequest request) {
		String endpoint = request.getRequestURL().toString().replace("-form", "");
		addModel("endpoint", endpoint);
		return new ModelAndView("transaccioncompleta/transaction-status-form", "model", getModel());
	}

	@RequestMapping(value = { "/refund" }, method = RequestMethod.POST)
	public ModelAndView refund(@RequestParam("token") String tokenWs, @RequestParam("amount") double amount,
			HttpServletRequest request) {
		log.info(String.format("token_ws : %s", tokenWs));

		cleanModel();
		addRequest("token_ws", tokenWs);
		addRequest("amount", amount);

		try {

			FullTransaction tx = new FullTransaction(new WebpayOptions(IntegrationCommerceCodes.TRANSACCION_COMPLETA,
					IntegrationApiKeys.WEBPAY, IntegrationType.TEST));
			final FullTransactionRefundResponse response = tx.refund(tokenWs, amount);

			log.debug(String.format("response : %s", response.getResponseCode()));
			addModel("response", response);
		} catch (IOException | TransactionRefundException e) {
			log.error(e.getLocalizedMessage(), e);
			return new ErrorController().error();
		}

		return new ModelAndView("transaccioncompleta/transaction-refund", "model", getModel());
	}

	@RequestMapping(value = "/refund-form", method = RequestMethod.GET)
	public ModelAndView refundForm(HttpServletRequest request) {
		String endpoint = request.getRequestURL().toString().replace("-form", "");
		addModel("endpoint", endpoint);
		return new ModelAndView("transaccioncompleta/transaction-refund-form", "model", getModel());
	}

}
