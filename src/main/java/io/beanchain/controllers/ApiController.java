package io.beanchain.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.beanpack.TXs.AirdropTX;
import io.beanchain.services.InternalTxFactory;
import io.beanchain.services.RewardDB;



@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/rn")
public class ApiController {
    

    @PostMapping("/faucet/drip")
    public ResponseEntity<?> faucetDrip(@RequestBody Map<String, String> request) throws Exception {
        String address = request.get("address");
        long timeLeft = RewardDB.getRemainingDripTime(address);

        if (!RewardDB.canDrip(address)) {
            return ResponseEntity.badRequest().body(
                "{\"status\": \"error\", \"message\": \"Cooldown time remaining: " + (timeLeft / 1000) + " seconds\"}"
            );
        }

        AirdropTX dripTX = InternalTxFactory.createFaucetDripTx(address);
        PeerConnector.sendTxToGPN(dripTX);
        RewardDB.updateLastDrip(address);

        return ResponseEntity.ok(
            "{\"status\": \"success\", \"message\": \"Drip incoming!\", \"dripAmount\": " + InternalTxFactory.calculateDrip() + "}"
        );
    }

    @GetMapping("/faucet/drip/{address}")
    public ResponseEntity<?> faucetDripByPath(@PathVariable("address") String address) throws Exception {
        long timeLeft = RewardDB.getRemainingDripTime(address);

        if (!RewardDB.canDrip(address)) {
            return ResponseEntity.badRequest().body(
                "{\"status\": \"error\", \"message\": \"Cooldown time remaining: " + (timeLeft / 1000) + " seconds\"}"
            );
        }

        AirdropTX dripTX = InternalTxFactory.createFaucetDripTx(address);
        PeerConnector.sendTxToGPN(dripTX);
        RewardDB.updateLastDrip(address);

        return ResponseEntity.ok(
            "{\"status\": \"success\", \"message\": \"Drip incoming!\", \"dripAmount\": " + InternalTxFactory.calculateDrip() + "}"
        );
    }

}
