/*
 * Copyright 2018 Loopring Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.lightcone.ethereum.contract;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * Auto generated code.
 *
 * <p><strong>Do not modify!</strong>
 *
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the <a
 * href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.0.1.
 */
public class WETH9 extends Contract {
  private static final String BINARY =
      "60c0604052600d60808190527f577261707065642045746865720000000000000000000000000000000000000060a090815261003e91600091906100a3565b506040805180820190915260048082527f57455448000000000000000000000000000000000000000000000000000000006020909201918252610083916001916100a3565b506002805460ff1916601217905534801561009d57600080fd5b5061013e565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106100e457805160ff1916838001178555610111565b82800160010185558215610111579182015b828111156101115782518255916020019190600101906100f6565b5061011d929150610121565b5090565b61013b91905b8082111561011d5760008155600101610127565b90565b6106f88061014d6000396000f3fe6080604052600436106100b9576000357c010000000000000000000000000000000000000000000000000000000090048063313ce56711610081578063313ce5671461022e57806370a082311461025957806395d89b411461028c578063a9059cbb146102a1578063d0e30db0146100b9578063dd62ed3e146102da576100b9565b806306fdde03146100c3578063095ea7b31461014d57806318160ddd1461019a57806323b872dd146101c15780632e1a7d4d14610204575b6100c1610315565b005b3480156100cf57600080fd5b506100d8610364565b6040805160208082528351818301528351919283929083019185019080838360005b838110156101125781810151838201526020016100fa565b50505050905090810190601f16801561013f5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801561015957600080fd5b506101866004803603604081101561017057600080fd5b50600160a060020a0381351690602001356103f2565b604080519115158252519081900360200190f35b3480156101a657600080fd5b506101af610458565b60408051918252519081900360200190f35b3480156101cd57600080fd5b50610186600480360360608110156101e457600080fd5b50600160a060020a0381358116916020810135909116906040013561045d565b34801561021057600080fd5b506100c16004803603602081101561022757600080fd5b5035610591565b34801561023a57600080fd5b50610243610626565b6040805160ff9092168252519081900360200190f35b34801561026557600080fd5b506101af6004803603602081101561027c57600080fd5b5035600160a060020a031661062f565b34801561029857600080fd5b506100d8610641565b3480156102ad57600080fd5b50610186600480360360408110156102c457600080fd5b50600160a060020a03813516906020013561069b565b3480156102e657600080fd5b506101af600480360360408110156102fd57600080fd5b50600160a060020a03813581169160200135166106af565b33600081815260036020908152604091829020805434908101909155825190815291517fe1fffcc4923d04b559f4d29a8bfc6cda04eb5b0d3c460751c2402c5c5cc9109c9281900390910190a2565b6000805460408051602060026001851615610100026000190190941693909304601f810184900484028201840190925281815292918301828280156103ea5780601f106103bf576101008083540402835291602001916103ea565b820191906000526020600020905b8154815290600101906020018083116103cd57829003601f168201915b505050505081565b336000818152600460209081526040808320600160a060020a038716808552908352818420869055815186815291519394909390927f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925928290030190a350600192915050565b303190565b600160a060020a03831660009081526003602052604081205482111561048257600080fd5b600160a060020a03841633148015906104c05750600160a060020a038416600090815260046020908152604080832033845290915290205460001914155b1561052057600160a060020a03841660009081526004602090815260408083203384529091529020548211156104f557600080fd5b600160a060020a03841660009081526004602090815260408083203384529091529020805483900390555b600160a060020a03808516600081815260036020908152604080832080548890039055938716808352918490208054870190558351868152935191937fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef929081900390910190a35060019392505050565b336000908152600360205260409020548111156105ad57600080fd5b33600081815260036020526040808220805485900390555183156108fc0291849190818181858888f193505050501580156105ec573d6000803e3d6000fd5b5060408051828152905133917f7fcf532c15f0a6db0bd6d0e038bea71d30d808c7d98cb3bf7268a95bf5081b65919081900360200190a250565b60025460ff1681565b60036020526000908152604090205481565b60018054604080516020600284861615610100026000190190941693909304601f810184900484028201840190925281815292918301828280156103ea5780601f106103bf576101008083540402835291602001916103ea565b60006106a833848461045d565b9392505050565b60046020908152600092835260408084209091529082529020548156fea165627a7a7230582056565d9b96c8f49914616e93e07072c8883fc8960109933654189546c36751360029";

  public static final String FUNC_NAME = "name";

  public static final String FUNC_APPROVE = "approve";

  public static final String FUNC_TOTALSUPPLY = "totalSupply";

  public static final String FUNC_TRANSFERFROM = "transferFrom";

  public static final String FUNC_WITHDRAW = "withdraw";

  public static final String FUNC_DECIMALS = "decimals";

  public static final String FUNC_BALANCEOF = "balanceOf";

  public static final String FUNC_SYMBOL = "symbol";

  public static final String FUNC_TRANSFER = "transfer";

  public static final String FUNC_DEPOSIT = "deposit";

  public static final String FUNC_ALLOWANCE = "allowance";

  public static final Event APPROVAL_EVENT =
      new Event(
          "Approval",
          Arrays.<TypeReference<?>>asList(
              new TypeReference<Address>(true) {},
              new TypeReference<Address>(true) {},
              new TypeReference<Uint256>() {}));;

  public static final Event TRANSFER_EVENT =
      new Event(
          "Transfer",
          Arrays.<TypeReference<?>>asList(
              new TypeReference<Address>(true) {},
              new TypeReference<Address>(true) {},
              new TypeReference<Uint256>() {}));;

  public static final Event DEPOSIT_EVENT =
      new Event(
          "Deposit",
          Arrays.<TypeReference<?>>asList(
              new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));;

  public static final Event WITHDRAWAL_EVENT =
      new Event(
          "Withdrawal",
          Arrays.<TypeReference<?>>asList(
              new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));;

  @Deprecated
  protected WETH9(
      String contractAddress,
      Web3j web3j,
      Credentials credentials,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
  }

  protected WETH9(
      String contractAddress,
      Web3j web3j,
      Credentials credentials,
      ContractGasProvider contractGasProvider) {
    super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
  }

  @Deprecated
  protected WETH9(
      String contractAddress,
      Web3j web3j,
      TransactionManager transactionManager,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
  }

  protected WETH9(
      String contractAddress,
      Web3j web3j,
      TransactionManager transactionManager,
      ContractGasProvider contractGasProvider) {
    super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
  }

  public RemoteCall<String> name() {
    final Function function =
        new Function(
            FUNC_NAME,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
    return executeRemoteCallSingleValueReturn(function, String.class);
  }

  public RemoteCall<TransactionReceipt> approve(String guy, BigInteger wad) {
    final Function function =
        new Function(
            FUNC_APPROVE,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.Address(guy),
                new org.web3j.abi.datatypes.generated.Uint256(wad)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteCall<BigInteger> totalSupply() {
    final Function function =
        new Function(
            FUNC_TOTALSUPPLY,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    return executeRemoteCallSingleValueReturn(function, BigInteger.class);
  }

  public RemoteCall<TransactionReceipt> transferFrom(String src, String dst, BigInteger wad) {
    final Function function =
        new Function(
            FUNC_TRANSFERFROM,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.Address(src),
                new org.web3j.abi.datatypes.Address(dst),
                new org.web3j.abi.datatypes.generated.Uint256(wad)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteCall<TransactionReceipt> withdraw(BigInteger wad) {
    final Function function =
        new Function(
            FUNC_WITHDRAW,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(wad)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteCall<BigInteger> decimals() {
    final Function function =
        new Function(
            FUNC_DECIMALS,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {}));
    return executeRemoteCallSingleValueReturn(function, BigInteger.class);
  }

  public RemoteCall<BigInteger> balanceOf(String param0) {
    final Function function =
        new Function(
            FUNC_BALANCEOF,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(param0)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    return executeRemoteCallSingleValueReturn(function, BigInteger.class);
  }

  public RemoteCall<String> symbol() {
    final Function function =
        new Function(
            FUNC_SYMBOL,
            Arrays.<Type>asList(),
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
    return executeRemoteCallSingleValueReturn(function, String.class);
  }

  public RemoteCall<TransactionReceipt> transfer(String dst, BigInteger wad) {
    final Function function =
        new Function(
            FUNC_TRANSFER,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.Address(dst),
                new org.web3j.abi.datatypes.generated.Uint256(wad)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteCall<TransactionReceipt> deposit(BigInteger weiValue) {
    final Function function =
        new Function(
            FUNC_DEPOSIT, Arrays.<Type>asList(), Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function, weiValue);
  }

  public RemoteCall<BigInteger> allowance(String param0, String param1) {
    final Function function =
        new Function(
            FUNC_ALLOWANCE,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.Address(param0),
                new org.web3j.abi.datatypes.Address(param1)),
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
    return executeRemoteCallSingleValueReturn(function, BigInteger.class);
  }

  public List<ApprovalEventResponse> getApprovalEvents(TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList =
        extractEventParametersWithLog(APPROVAL_EVENT, transactionReceipt);
    ArrayList<ApprovalEventResponse> responses =
        new ArrayList<ApprovalEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      ApprovalEventResponse typedResponse = new ApprovalEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.src = (String) eventValues.getIndexedValues().get(0).getValue();
      typedResponse.guy = (String) eventValues.getIndexedValues().get(1).getValue();
      typedResponse.wad = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<ApprovalEventResponse> approvalEventFlowable(EthFilter filter) {
    return web3j
        .ethLogFlowable(filter)
        .map(
            new io.reactivex.functions.Function<Log, ApprovalEventResponse>() {
              @Override
              public ApprovalEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues =
                    extractEventParametersWithLog(APPROVAL_EVENT, log);
                ApprovalEventResponse typedResponse = new ApprovalEventResponse();
                typedResponse.log = log;
                typedResponse.src = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.guy = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.wad =
                    (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
              }
            });
  }

  public Flowable<ApprovalEventResponse> approvalEventFlowable(
      DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(APPROVAL_EVENT));
    return approvalEventFlowable(filter);
  }

  public List<TransferEventResponse> getTransferEvents(TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList =
        extractEventParametersWithLog(TRANSFER_EVENT, transactionReceipt);
    ArrayList<TransferEventResponse> responses =
        new ArrayList<TransferEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      TransferEventResponse typedResponse = new TransferEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.src = (String) eventValues.getIndexedValues().get(0).getValue();
      typedResponse.dst = (String) eventValues.getIndexedValues().get(1).getValue();
      typedResponse.wad = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<TransferEventResponse> transferEventFlowable(EthFilter filter) {
    return web3j
        .ethLogFlowable(filter)
        .map(
            new io.reactivex.functions.Function<Log, TransferEventResponse>() {
              @Override
              public TransferEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues =
                    extractEventParametersWithLog(TRANSFER_EVENT, log);
                TransferEventResponse typedResponse = new TransferEventResponse();
                typedResponse.log = log;
                typedResponse.src = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.dst = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.wad =
                    (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
              }
            });
  }

  public Flowable<TransferEventResponse> transferEventFlowable(
      DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(TRANSFER_EVENT));
    return transferEventFlowable(filter);
  }

  public List<DepositEventResponse> getDepositEvents(TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList =
        extractEventParametersWithLog(DEPOSIT_EVENT, transactionReceipt);
    ArrayList<DepositEventResponse> responses =
        new ArrayList<DepositEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      DepositEventResponse typedResponse = new DepositEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.dst = (String) eventValues.getIndexedValues().get(0).getValue();
      typedResponse.wad = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<DepositEventResponse> depositEventFlowable(EthFilter filter) {
    return web3j
        .ethLogFlowable(filter)
        .map(
            new io.reactivex.functions.Function<Log, DepositEventResponse>() {
              @Override
              public DepositEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues =
                    extractEventParametersWithLog(DEPOSIT_EVENT, log);
                DepositEventResponse typedResponse = new DepositEventResponse();
                typedResponse.log = log;
                typedResponse.dst = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.wad =
                    (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
              }
            });
  }

  public Flowable<DepositEventResponse> depositEventFlowable(
      DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(DEPOSIT_EVENT));
    return depositEventFlowable(filter);
  }

  public List<WithdrawalEventResponse> getWithdrawalEvents(TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList =
        extractEventParametersWithLog(WITHDRAWAL_EVENT, transactionReceipt);
    ArrayList<WithdrawalEventResponse> responses =
        new ArrayList<WithdrawalEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      WithdrawalEventResponse typedResponse = new WithdrawalEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.src = (String) eventValues.getIndexedValues().get(0).getValue();
      typedResponse.wad = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<WithdrawalEventResponse> withdrawalEventFlowable(EthFilter filter) {
    return web3j
        .ethLogFlowable(filter)
        .map(
            new io.reactivex.functions.Function<Log, WithdrawalEventResponse>() {
              @Override
              public WithdrawalEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues =
                    extractEventParametersWithLog(WITHDRAWAL_EVENT, log);
                WithdrawalEventResponse typedResponse = new WithdrawalEventResponse();
                typedResponse.log = log;
                typedResponse.src = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.wad =
                    (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
              }
            });
  }

  public Flowable<WithdrawalEventResponse> withdrawalEventFlowable(
      DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(WITHDRAWAL_EVENT));
    return withdrawalEventFlowable(filter);
  }

  @Deprecated
  public static WETH9 load(
      String contractAddress,
      Web3j web3j,
      Credentials credentials,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    return new WETH9(contractAddress, web3j, credentials, gasPrice, gasLimit);
  }

  @Deprecated
  public static WETH9 load(
      String contractAddress,
      Web3j web3j,
      TransactionManager transactionManager,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    return new WETH9(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
  }

  public static WETH9 load(
      String contractAddress,
      Web3j web3j,
      Credentials credentials,
      ContractGasProvider contractGasProvider) {
    return new WETH9(contractAddress, web3j, credentials, contractGasProvider);
  }

  public static WETH9 load(
      String contractAddress,
      Web3j web3j,
      TransactionManager transactionManager,
      ContractGasProvider contractGasProvider) {
    return new WETH9(contractAddress, web3j, transactionManager, contractGasProvider);
  }

  public static RemoteCall<WETH9> deploy(
      Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
    return deployRemoteCall(WETH9.class, web3j, credentials, contractGasProvider, BINARY, "");
  }

  @Deprecated
  public static RemoteCall<WETH9> deploy(
      Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
    return deployRemoteCall(WETH9.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
  }

  public static RemoteCall<WETH9> deploy(
      Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
    return deployRemoteCall(
        WETH9.class, web3j, transactionManager, contractGasProvider, BINARY, "");
  }

  @Deprecated
  public static RemoteCall<WETH9> deploy(
      Web3j web3j,
      TransactionManager transactionManager,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    return deployRemoteCall(WETH9.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
  }

  public static class ApprovalEventResponse {
    public Log log;

    public String src;

    public String guy;

    public BigInteger wad;
  }

  public static class TransferEventResponse {
    public Log log;

    public String src;

    public String dst;

    public BigInteger wad;
  }

  public static class DepositEventResponse {
    public Log log;

    public String dst;

    public BigInteger wad;
  }

  public static class WithdrawalEventResponse {
    public Log log;

    public String src;

    public BigInteger wad;
  }
}
