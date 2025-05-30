# BeanChain Reward Node (RN)

> Work In Progress — This repository contains the source code and logic for the BeanChain Reward Node (RN), a key part of the decentralized reward and system transaction infrastructure.

---

## Overview

The **Reward Node (RN)** is a specialized network node in the [BeanChain](https://beanchain.io) ecosystem. It is responsible for generating and submitting **internal system transactions**, such as:

- Validator gas fee rewards  
- Faucet drips for new wallets  
- Early wallet airdrops  
- Staking trust score evaluations (coming soon)  

The RN runs independently from validator or public nodes but operates as a trusted infrastructure component within the **Bean Cluster**, maintained by the BeanChain Core Team.

---

## Purpose of This Repository

This repository is provided **for open-source transparency only**. It shows how the RN works, how reward transactions are constructed, and how system wallets are managed.

**This code is not intended to be independently built or run**. It is deeply integrated into the official mainnet infrastructure and relies on trust and permission layers within the Bean Cluster.

Any attempt to spoof or impersonate the Reward Node on the network will be **automatically rejected and banned by peer nodes**. All reward logic, TX construction, and signature enforcement are designed to prevent unauthorized use — spoofing attempts will fail.

---

## Responsibilities

The RN is designed to:

- Listen for new blocks across the network
- Analyze validator behavior and reward activity
- Submit signed reward transactions (TX) or system-level payloads
- Enforce cooldowns and eligibility windows for faucet/aerial distributions
- Integrate with stake lists and trust score signals (WIP)

It acts as the **executor of the system wallets** on the network and manages reward and distribution logic securely. During airdrop or faucet processes, it uses a nested **`fundWallet`** system to isolate and track available system funds designated for distribution.

The RN **does not** participate in consensus or block validation, but serves as a system-level automator.

---

## Project Architecture

- Built in Java using the BeanChain stack
- Receives and processes messages via the BeanChain P2P protocol
- Generates internal TXs using the same TX class structure as other nodes
- Operates using a dedicated wallet (secured in config)
- Tracks cooldowns, eligibility, and reward logs via LevelDB

---

## Requirements

- Java 21+
- [`BeanPack-java` (BeanCore)](https://github.com/BeanChain-Core/BeanCore/packages/2476357)
- A connected and synced public or validator node (GPN or PN)

---

## Current Capabilities

  
- ✅ Early wallet reward tracking and disbursement  
- ✅ Validator gas reward distribution (reads block metadata) 
- 🛠️Faucet drip handler with cooldown enforcement 
- 🛠️ Trust score generation (ping-based logic in progress)  
- 🛠️ Sync behavior and message routing enhancements  

---

## Deployment

**This implementation is not meant for public deployment.** The logic, access controls, and system wallet identities are tightly controlled within the official Bean Cluster.

If future network governance introduces support for third-party RNs, a permissioned registration flow will be established.

---

## Role in the Network

The RN is part of the **core infrastructure** of BeanChain. It:

- Provides critical reward services across all testnets and the mainnet
- Ensures validators are properly incentivized
- Helps onboard wallets through early funding and faucet mechanisms
- Will support contract-related trust scoring for validator weight in future upgrades

The **BeanChain Core Team** runs the official RN as part of the main **Bean Cluster**, ensuring transparent and consistent reward issuance across the network.

---

## Contact

Maintained by the **BeanChain Core Team**  
Powered by **Outlandish Creative LLC – Outlandish Tech Division**

Email: **team@limabean.xyz**  
Website: [beanchain.io](https://beanchain.io)

---

## Disclaimer

This repository is under active development and intended for review, transparency, and internal audit only.  
It will not function outside of the mainnet environment, and any unauthorized deployment attempts will fail and may result in peer-level banning.

---
