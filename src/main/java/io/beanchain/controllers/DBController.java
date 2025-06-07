package io.beanchain.controllers;

import org.springframework.web.bind.annotation.*;

import io.beanchain.services.RewardDB;



@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/drip")
public class DBController {


    public DBController(RewardDB db){
        
    }

}
